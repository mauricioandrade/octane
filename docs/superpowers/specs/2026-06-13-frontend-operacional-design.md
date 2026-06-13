# Octane — Frontend Operacional: Design Spec

**Data:** 2026-06-13  
**Escopo:** Frontend React completo para todas as operações já cobertas pelo backend (station, fueling, pricing). Não inclui autenticação nem módulos não implementados no backend.

---

## Objetivo

Construir o frontend operacional do Octane: uma SPA React que expõe 100% das operações do backend existente (cadastros de postos/bombas/bicos/combustíveis, preços, turnos, encerrantes, abastecimentos e histórico). Suporte a desktop e tablet. Sem autenticação nesta versão.

---

## Decisões de design

| Decisão | Escolha | Motivo |
|---|---|---|
| Layout | Sidebar com módulos expansíveis | Escala bem com novos módulos futuros (Vendas/NFC-e) |
| Tema | Light padrão + toggle slate dark | Light para uso geral; dark para preferência ou tablet em ambiente escuro |
| Posto ativo | Seletor no rodapé da sidebar, persiste em `localStorage` | Contextual — todas as operações derivam do posto ativo |
| Registro de abastecimento | Expansão inline por bico | Zero sobreposições; frentista vê todos os bicos e expande o que usou |
| Fechamento de turno | Sheet em 2 etapas: encerrantes → reconciliação | Obriga revisão da reconciliação antes de confirmar |
| Cadastros | Lista + sheet lateral (create/edit) | Padrão consistente em Postos, Bombas, Bicos e Combustíveis |
| Routing | React Router v6, rotas planas por módulo | Simples, sem over-engineering; station no contexto, não na URL |

---

## Stack adicionada ao frontend

O frontend já tem React 19, TypeScript 5, Vite 6 e TanStack Query 5.

| Lib | Versão | Uso |
|---|---|---|
| `react-router-dom` | v6 | Roteamento client-side |
| `tailwindcss` + `@tailwindcss/vite` | v3 | Estilização utility-first |
| `shadcn/ui` | latest | Componentes base: Button, Input, Select, Table, Sheet, Dialog, Badge, Skeleton |
| `react-hook-form` | v7 | Forms com controle eficiente |
| `zod` | v3 | Validação de schema nos forms |
| `@hookform/resolvers` | latest | Bridge RHF ↔ Zod |
| `clsx` + `tailwind-merge` | latest | Composição de classes Tailwind |
| `sonner` | latest | Toasts de erro/sucesso (integra com shadcn/ui) |
| `lucide-react` | latest | Ícones |

Sem estado global (Redux/Zustand). O posto ativo é o único estado compartilhado, gerenciado via `StationContext` (React context + `localStorage`).

---

## Estrutura de arquivos

```
frontend/src/
├── api/                        # Funções de fetch por recurso
│   ├── stations.ts             # getStations, createStation, updateStation, patchStatus
│   ├── pumps.ts                # getPumps, createPump, updatePump, patchStatus
│   ├── nozzles.ts              # getNozzles, createNozzle, updateNozzle, patchStatus
│   ├── fuels.ts                # getFuels, patchFuelStatus
│   ├── prices.ts               # getPrices, getPriceHistory, createPrice
│   ├── shifts.ts               # getShift, listShifts, openShift, closeShift
│   ├── readings.ts             # registerReading
│   └── fuelings.ts             # listFuelings, registerFueling, cancelFueling
├── components/
│   ├── layout/
│   │   ├── AppShell.tsx        # Layout raiz: sidebar + outlet
│   │   ├── Sidebar.tsx         # Nav, StationSelector, ThemeToggle
│   │   └── TopBar.tsx          # Título da seção + status do turno
│   ├── pista/
│   │   ├── ShiftStatus.tsx     # Banner de turno aberto/fechado
│   │   ├── OpenShiftSheet.tsx  # Sheet: nome frentista + encerrantes abertura
│   │   ├── CloseShiftSheet.tsx # Sheet 2 etapas: encerrantes → reconciliação
│   │   ├── NozzleList.tsx      # Lista de bicos com expansão inline
│   │   └── FuelingForm.tsx     # Form inline expandido por bico
│   ├── cadastros/
│   │   ├── CadastroSubnav.tsx  # Sub-sidebar: Postos/Bombas/Bicos/Combustíveis
│   │   ├── StationSheet.tsx    # Sheet criar/editar posto
│   │   ├── PumpSheet.tsx       # Sheet criar/editar bomba
│   │   ├── NozzleSheet.tsx     # Sheet criar/editar bico
│   │   └── StatusToggle.tsx    # Toggle ativo/inativo inline na tabela
│   ├── precos/
│   │   ├── PriceTable.tsx      # Tabela de preços vigentes
│   │   ├── NewPriceForm.tsx    # Mini-form lateral com prévia de variação
│   │   └── PriceHistoryModal.tsx # Modal histórico por combustível
│   └── historico/
│       ├── ShiftList.tsx       # Lista paginada de turnos com filtros
│       └── ShiftDetailModal.tsx# Modal: abastecimentos + reconciliação do turno
├── context/
│   └── StationContext.tsx      # Posto ativo: estado + persistência localStorage
├── hooks/
│   ├── useActiveStation.ts     # Lê/escreve StationContext
│   └── useShift.ts             # Query do turno aberto do posto ativo
├── lib/
│   ├── api-client.ts           # fetch base com baseURL e error handling
│   ├── utils.ts                # cn(), formatBRL(), formatLiters()
│   └── theme.ts                # Toggle dark/light, persiste em localStorage
├── pages/
│   ├── PistaPage.tsx
│   ├── PrecosPage.tsx
│   ├── CadastrosPage.tsx       # Wrapper com sub-routing
│   ├── PostosPage.tsx
│   ├── BombasPage.tsx
│   ├── BicosPage.tsx
│   ├── CombustiveisPage.tsx
│   └── HistoricoPage.tsx
├── App.tsx                     # Router + QueryProvider + StationProvider
├── main.tsx
└── QueryProvider.tsx
```

---

## Rotas

```
/                     → redirect → /pista
/pista                → PistaPage
/precos               → PrecosPage
/cadastros            → redirect → /cadastros/postos
/cadastros/postos     → PostosPage
/cadastros/bombas     → BombasPage
/cadastros/bicos      → BicosPage
/cadastros/combustiveis → CombustiveisPage
/historico            → HistoricoPage
```

---

## Módulos em detalhe

### Shell

O `AppShell` envolve todas as rotas. Renderiza `Sidebar` à esquerda (200px desktop, 60px ícones no tablet ≤768px) e `<Outlet>` à direita.

**Sidebar:**
- Logo "⛽ Octane" no topo
- Nav items: Pista / Preços / Cadastros (expansível com sub-itens) / Histórico
- Rodapé: `StationSelector` (dropdown com postos ativos) + `ThemeToggle` (light/slate dark)

**StationContext:** guarda `{ id, name }` do posto ativo. Ao carregar, lê `localStorage['octane-station']`. Se não existir e houver exatamente 1 posto, seleciona automaticamente. Se houver mais de 1, abre o seletor de posto forçando a escolha.

**TopBar:** renderiza o título da página atual + badge de turno (verde "● Aberto · João" ou amarelo "○ Sem turno").

---

### Pista

**Sem turno aberto:** tela centralizada com botão "Abrir turno" → abre `OpenShiftSheet`.

**`OpenShiftSheet`:**  
- Campo: nome do frentista (obrigatório, max 100 chars)  
- Para cada bico ativo do posto: input de encerrante de abertura (BigDecimal, 3 casas decimais)  
- POST `/api/shifts` → POST `/api/shifts/{id}/readings` (tipo OPENING) para cada bico  
- Em caso de erro no segundo POST, exibe erro sem fechar o sheet

**Com turno aberto:** `TopBar` mostra badge verde + botão "Fechar turno". Conteúdo principal:  
- 4 cards de métricas: Volume total / Receita / Qtd. abastecimentos / Último abastecimento  
- `NozzleList`: lista todos os bicos ativos do posto, agrupados por bomba  
  - Cada bico mostra: número, combustível, preço/L, subtotais do turno  
  - Botão "+ Registrar" expande `FuelingForm` inline (só 1 bico expandido por vez)

**`FuelingForm` (inline):**  
- Input de litros (BigDecimal 3 dec.) → total calculado automaticamente: `litros × preço_vigente`  
- Seleção de forma de pagamento: PIX / Dinheiro / Débito / Crédito / Frota / Voucher (chips selecionáveis)  
- Campo placa (opcional, max 10 chars)  
- Botão "Confirmar" → POST `/api/shifts/{shiftId}/fuelings`  
- Invalida query do turno ao confirmar → métricas atualizam

**`CloseShiftSheet` (2 etapas):**  
- **Etapa 1:** para cada bico ativo, input de encerrante de fechamento. Pré-preenche com valor da leitura de abertura para evitar erro.  
  - Botão "Calcular reconciliação →" → POST `/api/shifts/{id}/readings` (tipo CLOSING) para cada bico → GET `/api/shifts/{id}/reconciliation`  
- **Etapa 2:** tabela de reconciliação por bico (medido / lançado / divergência). Badge semafórico: verde = 0, amarelo = >0 e ≤0,6% do medido, vermelho = >0,6%. Total de receita do turno. Botão "Confirmar fechamento" → POST `/api/shifts/{id}/close`.

---

### Preços

Conteúdo em dois painéis lado a lado:  
- **Esquerda:** tabela de preços vigentes (combustível / preço / vigente desde / link histórico). Dados de GET `/api/stations/{id}/prices`.  
- **Direita:** mini-form "Atualizar preço": select de combustível + input de novo preço. Ao digitar o preço, exibe prévia da variação (valor e % em relação ao vigente). Confirmar → POST `/api/stations/{id}/prices`. Invalida query de vigentes.  
- Link "Ver ›" na tabela abre `PriceHistoryModal`: tabela cronológica de GET `/api/stations/{id}/prices/history?fuelId={id}`.

---

### Cadastros

Sub-sidebar fixa com 4 itens: Postos / Bombas / Bicos / Combustíveis.

**Padrão comum:** lista com colunas relevantes + coluna de ações (editar + toggle status). Botão "+" no topo abre Sheet lateral de criação. Clicar em ✏️ abre o mesmo Sheet em modo edição com dados pré-preenchidos.

**Postos:** colunas Nome / CNPJ / Cidade / Status. Form: nome, CNPJ, endereço, cidade, UF.

**Bombas:** contextuais ao posto ativo. Colunas: Número / Status (ACTIVE/INACTIVE/MAINTENANCE). Form: número. Status alterado via um select inline na coluna (dropdown com 3 opções: Ativa / Inativa / Manutenção) — PATCH `/api/pumps/{id}/status` ao mudar.

**Bicos:** contextuais ao posto ativo. Filtro por bomba. Colunas: Número / Bomba / Combustível / Status. Form: número, bomba (select das bombas ativas), combustível (select dos combustíveis ativos).

**Combustíveis:** lista global (não depende do posto). Colunas: Nome / Unidade / Status. Sem criação — apenas ativar/inativar via toggle.

---

### Histórico

Lista paginada de turnos do posto ativo. GET `/api/stations/{id}/shifts?page=&size=10&from=&to=&status=`.  
Filtros: intervalo de datas (date picker) + status (Aberto / Fechado / Todos).  
Colunas: Frentista / Abertura / Duração / Volume / Receita / Status.

Clicar em uma linha abre `ShiftDetailModal`:  
- Resumo do turno (frentista, horários, totais)  
- Lista de abastecimentos do turno (GET `/api/shifts/{id}/fuelings`)  
- Relatório de reconciliação se turno fechado (GET `/api/shifts/{id}/reconciliation`)

---

## Tratamento de erros e estados

- **Loading:** Skeleton (shadcn) em todas as tabelas e listas durante fetch inicial
- **Erro de API:** toast de erro não-bloqueante via `sonner` (integra nativamente com shadcn/ui)
- **Formulários:** validação Zod client-side antes do POST; erros exibidos abaixo do campo
- **Sem posto selecionado:** redirect forçado ao seletor antes de qualquer operação
- **Posto sem turno aberto tentando registrar abastecimento:** não ocorre — a NozzleList só é exibida quando o turno está aberto

---

## Decisão de design: arquitetura futura NFC-e

O módulo `retail` (NFC-e) será uma seção "Vendas" separada na sidebar. O fluxo de venda será **por produto** (combustível como item de NCM 2710 + produtos não-combustível), não por bico. A ligação ao bico/turno para fins de LMC ficará em segundo plano, possivelmente automatizada via integração com a bomba.

Isso mantém o módulo `fueling` atual como a fonte de verdade para o LMC/ANP, enquanto o módulo `retail` cuida da relação comercial com o cliente (NFC-e, troco, formas de pagamento).
