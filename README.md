# Octane

Sistema de gestão para postos de combustível — cobrindo pista, preços, cadastros e turnos com reconciliação ANP.

## Stack

| Camada | Tecnologia |
|--------|-----------|
| Backend | Java 21, Spring Boot 4, Maven |
| Frontend | Vite 6, React 19, TypeScript 5, TanStack Query 5 |
| Banco | PostgreSQL 16 (Docker) |
| Arquitetura | Clean Architecture (domain → use case → handler) |

## Pré-requisitos

- Java 21+
- Node 20+
- Docker

## Setup local

```bash
make dev-db      # sobe PostgreSQL via Docker Compose
make backend     # compila e inicia o Spring Boot (porta 8080)
make frontend    # inicia o Vite dev server (porta 5173)
```

Credenciais padrão: `admin / octane123` (configurável via `ADMIN_USERNAME` / `ADMIN_PASSWORD`).

## Funcionalidades implementadas

### ⛽ Pista e Turnos
| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/shifts` | Abre turno no posto |
| POST | `/api/shifts/{id}/close` | Fecha turno com reconciliação de encerrantes + calcula comissão automaticamente |
| GET | `/api/shifts/{id}/reconciliation` | Exibe divergências por bico (LMC) |
| POST | `/api/shifts/{shiftId}/fuelings` | Registra abastecimento |
| POST | `/api/shifts/{shiftId}/fuelings/{fuelingId}/cancel` | Cancela abastecimento |
| POST | `/api/shifts/{id}/readings` | Registra leitura de encerrante (abertura/fechamento) |

### 🏪 Cadastros Base
| Método | Rota | Descrição |
|--------|------|-----------|
| GET/POST | `/api/stations` | Lista e cadastra postos |
| GET/PUT/PATCH | `/api/stations/{id}` | Detalha, atualiza e ativa/desativa posto |
| GET/POST | `/api/stations/{id}/pumps` | Lista e cadastra bombas |
| GET/POST | `/api/pumps/{id}/nozzles` | Lista e cadastra bicos |
| GET/POST/PATCH | `/api/fuels` | Combustíveis (tipo, unidade, status) |

### 💰 Preços
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/api/prices/current` | Preços vigentes por combustível |
| POST | `/api/prices` | Define novo preço com histórico |
| GET | `/api/prices/history` | Histórico de alterações |

### 🚗 Controle de Frota (V14 — Migrations V12-V14)
Clientes PJ com limite mensal, veículos com restrição de combustível, motoristas identificados por CPF, PIN ou RFID.

| Método | Rota | Descrição |
|--------|------|-----------|
| GET/POST | `/api/fleet/clients` | Lista e cadastra clientes de frota |
| GET/PUT | `/api/fleet/clients/{id}` | Detalha e atualiza cliente |
| POST | `/api/fleet/vehicles` | Cadastra veículo |
| GET | `/api/fleet/clients/{clientId}/vehicles` | Lista veículos por cliente |
| GET/PUT | `/api/fleet/vehicles/{id}` | Detalha e atualiza veículo |
| POST | `/api/fleet/drivers` | Cadastra motorista |
| GET | `/api/fleet/clients/{clientId}/drivers` | Lista motoristas por cliente |
| GET/PUT | `/api/fleet/drivers/{id}` | Detalha e atualiza motorista |
| POST | `/api/fleet/drivers/identify` | Identifica motorista (CPF/PIN/RFID) |
| GET/POST | `/api/fleet/fuelings` | Lista e registra abastecimentos de frota |
| GET | `/api/fleet/clients/{clientId}/fuelings` | Abastecimentos por cliente |
| GET | `/api/fleet/reports/consumption` | Relatório de consumo por cliente |
| GET | `/api/fleet/reports/consumption/csv` | Exporta relatório em CSV |

Frontend: `/frota/clientes`, `/frota/clientes/:id`, `/frota/veiculos`, `/frota/motoristas`, `/frota/relatorio`

### 🔧 Ordem de Serviço — OS (Migrations V15-V17)
OS digital com placa, quilometragem, itens (peças/serviços), histórico por veículo e cancelamento rastreado.

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/service-orders` | Cria ordem de serviço |
| GET | `/api/stations/{stationId}/service-orders` | Lista OS por posto (filtros: status, from, to) |
| GET | `/api/service-orders/{id}` | Detalha OS |
| POST | `/api/service-orders/{id}/items` | Adiciona item (peça ou serviço) |
| POST | `/api/service-orders/{id}/close` | Fecha OS |
| POST | `/api/service-orders/{id}/cancel` | Cancela OS (registra `cancelledAt`) |
| GET | `/api/service-orders/by-plate/{plate}` | Histórico por placa |

Frontend: `/os`, `/os/:id`, `/os/historico`

### 💼 Comissão de Funcionários (Migrations V18-V19)
Regras de comissão por funcionário (taxa % sobre o total do turno), cálculo automático ao fechar turno, controle de pagamento.

| Método | Rota | Descrição |
|--------|------|-----------|
| GET/POST | `/api/commission/rules` | Lista e cria regras de comissão |
| PUT | `/api/commission/rules/{id}` | Atualiza regra |
| PATCH | `/api/commission/rules/{id}/status` | Ativa/desativa regra |
| POST | `/api/commission/calculate/{shiftId}` | Calcula comissão de um turno |
| GET | `/api/commission/entries` | Lista entradas (filtros: paid, from, to) |
| GET | `/api/commission/shifts/{shiftId}/entry` | Comissão de um turno específico |
| POST | `/api/commission/entries/{id}/pay` | Marca comissão como paga |

Frontend: `/comissao/regras`, `/comissao/entradas`

### Infra
| Método | Rota | Descrição |
|--------|------|-----------|
| GET | `/health` | Health check |

---

## Visão do Produto — Posto Completo

Levantamento das funcionalidades obrigatórias para cada área de um posto completo, com base normativa e referências de mercado.

### 🏪 Loja de Conveniência (PDV Ágil)

**Controle de Lote e Validade**
Registra cada entrada com número de lote e data de validade, aplica PEPS automaticamente e dispara alertas preventivos para produtos próximos ao vencimento. Bloqueia venda de itens vencidos e mantém rastreabilidade.
Base legal: [RDC 655/2022 – ANVISA](https://anvisalegis.datalegis.net/action/ActionDatalegis.php?acao=abrirTextoAto&link=S&tipo=RDC&numeroAto=00000655&seqAto=000&valorAno=2022&orgao=RDC/DC/ANVISA/MS&cod_modulo=310&cod_menu=9431) (rastreabilidade obrigatória por 6 meses após o vencimento). Ref: [Webmais Sistemas](https://webmaissistemas.com.br/controle-lote-validade/)

**Comanda Eletrônica**
Permite que o cliente consuma na padaria/cafeteria e pague tudo junto (combustível + consumo) em um único caixa. Elimina retrabalho e erros de fechamento entre setores.
Refs: [Linx para Padarias](https://mkt.linx.com.br/sistema-linx-para-padarias-e-emporios) · [CS Automação PDV Móvel](https://www.csautomacao.com.br/pdv-smart-movel/)

**Curva ABC Automática**
Classifica o mix (tipicamente 1.000–3.000 SKUs) em A/B/C por faturamento real, orientando reposição e enxugamento do portfólio. Geração agendada semanal/quinzenal/mensal.
Refs: [Linx AutoSystem](https://www.linx.com.br/linx-autosystem/) · [TOTVS – O que é Curva ABC](https://www.totvs.com/blog/negocios/curva-abc/)

**Frente de Caixa Rápido**
Integração com balanças homologadas pelo INMETRO, leitores omnidirecionais e terminais de autoatendimento (self-checkout). Reduz filas nos horários de pico.
Refs: [FeComércio – autoatendimento no Brasil](https://www.fecomercio.com.br/noticia/caixas-de-autoatendimento-ganham-espaco-no-brasil) · [Sanvitron – autoatendimento em postos](https://sanvitron.com.br/autoatendimento-em-postos-de-combustivel/)

---

### 🚗 Centro de Serviços (Troca de Óleo e Lava-Rápido)

**Ordem de Serviço (OS) Digital**
Registra placa, quilometragem atual e checklist de itens revisados. Concilia automaticamente o serviço prestado com o estoque baixado e gera histórico rastreável por placa.
Refs: [Inforlube](https://www.inforlube.com/) · [GestãoClick – OS para Oficina](https://gestaoclick.com.br/ordem-de-servico-para-oficina-mecanica/)

**Histórico do Veículo**
Vincula cada OS encerrada à placa. Na próxima visita (ou proativamente via SMS/WhatsApp), alerta que o intervalo de troca de óleo por km ou meses está próximo — transformando atendimento reativo em receita recorrente.
Refs: [Inforlube Desktop](https://www.inforlube.com/solucoes/InforlubeDesktop) · [ClubPetro – como triplicar vendas de lubrificantes](https://blog.clubpetro.com/lubrificantes-como-tripliquei-as-vendas/)

**Comissão de Funcionários**
Calcula automaticamente a comissão individual por turno com base nos serviços executados ou produtos vendidos, integrado ao fechamento de caixa. Vincula remuneração variável ao desempenho medido.
Refs: [CIGAM ERP para Postos](https://www.cigam.com.br/postos-de-combustiveis) · [Adaptive – Fechamento de Caixa em Posto](https://adaptive.com.br/dicas-para-abertura-e-fechamento-de-caixa-em-posto-de-combustivel/)

---

### ⛽ Pista de Combustível Avançada

**Identificação do Frentista (RFID / Biometria)**
Cada frentista apresenta cartão RFID ou biometria antes de a bomba ser liberada. Bloqueia abastecimentos não autorizados e rastreia produtividade por operador.
Refs: [Gilbarco Prime ID](https://www.gilbarco.com/br/solucoes/solucoes-para-o-varejo%20/prime-id) · [RS AutoSystem – IdentFID](http://www.rsauto.com.br/software/identfid.html)

**Medição de Tanques Real (Telemetria)**
Sondas eletrônicas (ultrassônicas ou magnetostritivas) reportam volume, temperatura e densidade dos tanques em tempo real. O sistema cruza estoque inicial + recebimentos − encerrantes das bombas = estoque teórico vs. físico. Divergências acima de **0,6%** disparam alertas automáticos.
Base legal: [Resolução ANP 884/2022](https://www.legisweb.com.br/legislacao/?id=436089) + [Portaria INMETRO 227/2022](https://www.legisweb.com.br/legislacao/?id=432072) (medição manual por régua proibida desde setembro/2023).
Refs: [Gilbarco TLS](https://blog.gilbarco.com/medicao-eletronica-de-combustivel-nos-tanques-como-funciona-e-quais-beneficios) · [Telemed](https://www.telemed.com.br/medicao-de-tanques/) · [Brasil Postos – medição eletrônica obrigatória](https://www.brasilpostos.com.br/noticias/equipamentos/atencao-sistema-de-medicao-eletronica-comecara-a-ser-exigido-em-todos-os-postos-do-pais/)

**Preço Dinâmico**
Alteração de preço no sistema propaga simultaneamente para todas as bombas (via concentrador RS-485) e para os painéis LED/tótem da pista — sem intervenção manual em cada equipamento.
Refs: [Pricetech – Techposto](https://www.loja.techposto.com.br/painel-de-precos) · [Valrem – Totem de Preço](https://valrem.com.br/produtos/Totem-de-Preco-para-Posto-de-Combustibel/)

---

### 💼 Gestão de Retaguarda (ERP) e Fiscal

**Conciliação Automática de Recebíveis**
Importa extratos das adquirentes (Cielo, Rede, Stone) via CNAB/OFX ou API, confronta cada transação com o lançamento no PDV e identifica divergências de prazo, taxa e estorno.
Base legal: EFD-ICMS/IPI exige escrituração correta de todas as vendas (Decreto 7.212/2010).
Refs: [Cielo Conciliador](https://www.cielo.com.br/conciliacao-financeira/) · [TOTVS Varejo Postos](https://produtos.totvs.com/ficha-tecnica/tudo-sobre-o-totvs-varejo-postos-de-combustiveis/)

**SPED Fiscal e LMC**
O Livro de Movimentação de Combustíveis (LMC) registra diariamente entradas (NF de compra), saídas por bomba e variações de estoque. Formato 100% eletrônico permitido desde outubro/2022; guarda mínima de 6 meses no estabelecimento. LMC e EFD coexistem — um não substitui o outro.
Base legal: [Resolução ANP 884/2022](https://www.legisweb.com.br/legislacao/?id=436089) · [FAQ ANP – LMC](https://www.gov.br/anp/pt-br/acesso-a-informacao/perguntas-frequentes/agente-economico/livro-de-movimentacao-de-combustiveis).
Refs: [Adaptive – como preencher o LMC](https://adaptive.com.br/o-que-e-e-como-preencher-o-livro-de-movimentacao-de-combustiveis-lmc/) · [Brasil Postos – LMC eletrônico](https://www.brasilpostos.com.br/noticias/fiscalizacao-2/lmc-apenas-eletronico-isso-ja-e-possivel/)

**Controle de Frota (Clientes PJ)**
Identifica motorista (CPF, RFID ou PIN), placa e hodômetro no momento do abastecimento. Aplica restrições de tipo de combustível e limite de volume/valor por período configuradas por cliente ou veículo.
Refs: [RotaExata – integração com postos](https://www.rotaexata.com.br/blog/controle-de-abastecimento-da-frota-rotaexata-ticket-log/) · [Abastek – controle com detecção de fraudes](https://abastek.com/controle-abastecimento-frota/)

---

## Desenvolvimento

Convenção de commits: [docs/commit-conventions.md](docs/commit-conventions.md)

```bash
./mvnw test          # roda os 162 testes unitários do backend
npm run typecheck    # verificação de tipos do frontend (pasta frontend/)
```

---

## Estado atual do desenvolvimento

### Feito

- [x] Cadastros base (postos, bombas, bicos, combustíveis)
- [x] Preços com histórico
- [x] Pista: abertura de turno, abastecimento, leitura de encerrante, fechamento com reconciliação ANP
- [x] Dark mode, autenticação, layout responsivo mobile
- [x] Exportação CSV (relatório de frota)
- [x] Controle de Frota — backend + frontend completo (Migrations V12-V14)
- [x] Ordem de Serviço — backend + frontend completo (Migrations V15-V17)
- [x] Comissão de Funcionários — backend + frontend completo (Migrations V18-V19)

### Pendente / Próximos módulos

- [ ] **Review dos 3 módulos novos** — revisar Frota, OS e Comissão em conjunto para consistência e bugs remanescentes
- [ ] **Testes de integração** — subir banco real e verificar se as migrations V12-V19 rodam sem erro
- [ ] **Loja de Conveniência (PDV)** — estoque, lote, validade, frente de caixa
- [ ] **Medição de Tanques** — telemetria, estoque físico vs. teórico, alertas de divergência (> 0,6%)
- [ ] **SPED Fiscal / LMC eletrônico** — exportação no formato ANP
- [ ] **Conciliação de Recebíveis** — importação de extrato de adquirentes (Cielo, Rede, Stone)
- [ ] **Preço Dinâmico** — propagação para bombas e painéis LED
- [ ] **Notificações proativas** — SMS/WhatsApp para intervalo de troca de óleo por km/meses
