# Octane — Núcleo de Pista (Backend) — Design

**Data:** 2026-06-12
**Status:** Aprovado para planejamento

## Contexto

O backend cobre hoje cadastros base (posto, bomba, bico, combustível) e o ciclo de
abastecimento (turno, leitura de encerrante, abastecimento), mas o domínio de pista
tem lacunas: preço é digitado livremente a cada abastecimento, não há conciliação
entre encerrantes e abastecimentos, cadastros não podem ser editados nem inativados,
abastecimento errado não pode ser corrigido e as listagens não têm paginação nem
filtros. Este subprojeto fecha essas lacunas antes do frontend.

Decomposição acordada do roadmap: **1) núcleo de pista no backend (este design)**,
2) frontend operacional, 3) autenticação/autorização, 4) estoque, 5) financeiro.
Cada um terá spec própria.

## Decisões de produto

| Tema | Decisão |
|---|---|
| Preço | Vem da tabela de preços vigentes; frentista não digita preço |
| Conciliação | Apurada e persistida no fechamento do turno; nunca bloqueia |
| Cadastros | Editar + inativar (soft); nunca deletar fisicamente |
| Estorno | Cancelamento de abastecimento apenas com turno OPEN |
| Paginação | Só em turnos e abastecimentos; cadastros ganham apenas filtros |
| Arquitetura | Preço em módulo próprio `com.octane.pricing` |

## 1. Módulo `pricing`

Novo pacote `com.octane.pricing`, mesmo padrão dos módulos existentes
(domain / domain/repository / usecase / repository / handler), Clean Architecture,
sem Lombok, injeção via construtor, Records para DTOs.

### Migration `V8__create_fuel_prices.sql`

```sql
CREATE TABLE fuel_prices (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    station_id UUID NOT NULL REFERENCES stations(id),
    fuel_id UUID NOT NULL REFERENCES fuels(id),
    price NUMERIC(10,4) NOT NULL,
    effective_from TIMESTAMP NOT NULL DEFAULT NOW(),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_fuel_prices_lookup
    ON fuel_prices (station_id, fuel_id, effective_from DESC);
```

A tabela é **append-only**: cadastrar um preço novo torna o anterior histórico.
Preço vigente = linha com maior `effective_from` para o par (posto, combustível).
Vigência é sempre imediata (`effective_from = now()`).

### Domínio

- `FuelPrice`: `id`, `station` (ManyToOne), `fuel` (ManyToOne), `price` (BigDecimal),
  `effectiveFrom`, `createdAt`.
- `FuelPriceRepository` (interface pura no domínio):
  `save`, `findCurrent(stationId, fuelId)`, `findCurrentByStation(stationId)`,
  `findHistory(stationId, fuelId)`.

### Use cases

- `SetFuelPriceUseCase` — valida preço > 0, posto existe e está ativo, combustível
  existe e está ativo; grava nova linha.
- `GetCurrentPricesUseCase` — preço vigente de cada combustível com preço cadastrado
  no posto.
- `ListPriceHistoryUseCase` — histórico de um combustível no posto, mais recente
  primeiro.

### Endpoints (`FuelPriceHandler`)

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/stations/{stationId}/prices` | Cadastrar preço (body: `fuelId`, `price`) |
| GET | `/api/stations/{stationId}/prices` | Preços vigentes do posto |
| GET | `/api/stations/{stationId}/prices/history?fuelId=` | Histórico de um combustível |

### Fora de escopo (YAGNI)

Preço com vigência futura agendada, promoções, descontos por forma de pagamento.

## 2. Abastecimento integrado ao preço + cancelamento

### Mudança no `RegisterFuelingUseCase`

`RegisterFuelingRequest` deixa de receber `unitPrice`. Novo contrato:

```java
public record RegisterFuelingRequest(
    UUID nozzleId,
    BigDecimal liters,       // opcional se totalAmount informado
    BigDecimal totalAmount,  // opcional se liters informado
    String paymentMethod,
    String vehiclePlate,
    String notes
) {}
```

Fluxo:
1. Valida turno OPEN e bico pertencente ao posto do turno (regras já existentes).
2. Valida cadeia ativa: bico ativo, bomba `ACTIVE`, posto ativo → senão 422.
3. Busca o preço vigente do combustível do bico no posto; sem preço → 422.
4. Calcula o campo ausente: `liters = totalAmount / price` (3 casas, HALF_UP) ou
   `totalAmount = liters × price` (2 casas, HALF_UP). Nenhum dos dois informado → 422.
   Os dois informados → valida consistência com tolerância de R$ 0,01.
5. Grava o abastecimento com `unit_price` preenchido como snapshot do preço vigente
   (coluna existente, sem migration).

### Cancelamento

Migration `V9__add_status_to_fuelings.sql`:

```sql
ALTER TABLE fuelings
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN canceled_at TIMESTAMP;
```

- Enum `FuelingStatus { ACTIVE, CANCELED }` no domínio.
- `CancelFuelingUseCase` — valida que o abastecimento existe, pertence ao turno da
  rota, está `ACTIVE` e o turno está `OPEN`; marca `CANCELED` + `canceledAt = now()`.
  Turno fechado ou já cancelado → 422.
- Endpoint: `POST /api/shifts/{shiftId}/fuelings/{id}/cancel`.
- Abastecimentos `CANCELED` ficam no banco para auditoria, mas saem dos totais do
  turno e da conciliação. `ListFuelingsByShiftUseCase` retorna apenas `ACTIVE` por
  padrão (sem filtro para incluir cancelados neste escopo).

## 3. Conciliação de turno

Migration `V10__create_shift_reconciliations.sql`:

```sql
CREATE TABLE shift_reconciliations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    shift_id UUID NOT NULL REFERENCES shifts(id),
    nozzle_id UUID NOT NULL REFERENCES nozzles(id),
    opening_totalizer NUMERIC(12,3) NOT NULL,
    closing_totalizer NUMERIC(12,3) NOT NULL,
    measured_liters NUMERIC(12,3) NOT NULL,
    fueled_liters NUMERIC(12,3) NOT NULL,
    divergence_liters NUMERIC(12,3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (shift_id, nozzle_id)
);
```

- `CloseShiftUseCase` ganha uma etapa, na mesma transação do fechamento: para cada
  bico que tem leituras `OPENING` e `CLOSING` no turno (cobre bico inativado no
  meio do turno), calcula `measured = closing − opening`,
  `fueled = Σ liters` dos abastecimentos `ACTIVE` daquele bico no turno, e
  `divergence = measured − fueled`. Persiste uma linha por bico. Divergência nunca
  bloqueia o fechamento.
- `GetShiftReconciliationUseCase` + `GET /api/shifts/{id}/reconciliation` —
  retorna as linhas por bico (com número do bico e nome do combustível) e os
  totais do turno (litros medidos, litros lançados, divergência). Disponível
  apenas para turno `CLOSED`; turno aberto → 422.

## 4. Edição e inativação de cadastros

Nenhuma migration necessária (colunas `active`/`status` já existem). Nada é
deletado fisicamente.

| Entidade | Endpoint | Regras |
|---|---|---|
| Posto | `PUT /api/stations/{id}` | Atualiza todos os campos; CNPJ revalidado contra duplicidade (ignorando o próprio registro) |
| Posto | `PATCH /api/stations/{id}/status` body `{active}` | Liga/desliga |
| Bomba | `PUT /api/pumps/{id}` | Atualiza número; valida duplicidade no posto |
| Bomba | `PATCH /api/pumps/{id}/status` body `{status}` | `ACTIVE` / `INACTIVE` / `MAINTENANCE` |
| Bico | `PUT /api/nozzles/{id}` | Atualiza número e combustível; valida duplicidade na bomba e combustível ativo |
| Bico | `PATCH /api/nozzles/{id}/status` body `{active}` | Liga/desliga |
| Combustível | `PATCH /api/fuels/{id}/status` body `{active}` | Cadastro de combustível segue só via seed |

Use cases novos: `UpdateStationUseCase`, `UpdateStationStatusUseCase`,
`UpdatePumpUseCase`, `UpdatePumpStatusUseCase`, `UpdateNozzleUseCase`,
`UpdateNozzleStatusUseCase`, `UpdateFuelStatusUseCase`.

Efeitos transversais: a validação de cadeia ativa no `RegisterFuelingUseCase`
(seção 2) é o que dá efeito prático à inativação; `OpenShiftUseCase` passa a
validar que o posto está ativo (422 se inativo). `CloseShiftUseCase` já exige
leitura `CLOSING` apenas de bicos ativos — comportamento mantido.

## 5. Paginação e filtros

- Record genérico em `shared/`:

```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages
) {}
```

- `GET /api/stations/{stationId}/shifts` → paginado (`page`, `size`, default
  `0`/`20`), filtros opcionais `from`/`to` (sobre `openedAt`) e `status`.
- `GET /api/shifts/{shiftId}/fuelings` → paginado; `totalLiters` e `totalAmount`
  do `ShiftSummaryResponse` passam a ser calculados por query agregada sobre
  **todos** os abastecimentos `ACTIVE` do turno (não só a página).
- Cadastros: `GET /api/stations?active=`, `GET /api/stations/{id}/pumps?status=`,
  `GET /api/pumps/{id}/nozzles?active=`, `GET /api/fuels?active=` — apenas
  filtros opcionais, sem paginação. Sem parâmetro, comportamento atual (lista tudo).

Os métodos de repositório de domínio recebem os parâmetros de filtro/página de
forma explícita (sem vazar `Pageable` para a interface de domínio); a conversão
para Spring Data acontece nas implementações.

## Tratamento de erros e testes

- Exceções existentes: `EntityNotFoundException` → 404, `BusinessException` → 422,
  validação → 400.
- `BigDecimal` para todos os valores monetários e de litros.
- Testes unitários por use case (casos felizes + cada caminho de erro) e por
  handler, no mesmo padrão dos módulos existentes.

## Ordem sugerida de implementação

1. Edição/inativação de cadastros (independente, destrava o resto).
2. Módulo `pricing`.
3. Integração do abastecimento com preço + cancelamento.
4. Conciliação no fechamento + endpoint de relatório.
5. Paginação e filtros.
