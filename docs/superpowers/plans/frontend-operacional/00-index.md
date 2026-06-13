# Octane — Frontend Operacional: Índice do Plano

> **Para agentes:** use `superpowers:executing-plans` ou `superpowers:subagent-driven-development` para executar cada task.

**Goal:** SPA React completa para todas as operações do backend (postos, bombas, bicos, combustíveis, preços, turnos, encerrantes, abastecimentos, histórico).

**Architecture:** React 19 + React Router v6 + TanStack Query 5 + Tailwind CSS v3 + shadcn/ui. StationContext (localStorage) é o único estado global. Vite proxia `/api/*` → Spring Boot :8080.

**Tech Stack:** React 19, TypeScript 5, Vite 6, TanStack Query 5, react-router-dom v6, tailwindcss v3, shadcn/ui, react-hook-form v7, zod v3, @hookform/resolvers, clsx, tailwind-merge, sonner, lucide-react

---

## Tasks

| # | Arquivo | Conteúdo |
|---|---------|----------|
| 01 | [01-setup.md](01-setup.md) | npm deps + Tailwind v3 + shadcn init + componentes |
| 02 | [02-infra.md](02-infra.md) | Vite proxy + `@/` alias + `api-client.ts` + `types.ts` |
| 03 | [03-foundation.md](03-foundation.md) | StationContext + `lib/utils.ts` + `lib/theme.ts` + hooks |
| 04 | [04-shell.md](04-shell.md) | AppShell + Sidebar + TopBar + App.tsx routing + page stubs |
| 05 | [05-api-layer.md](05-api-layer.md) | Todas as funções `src/api/*.ts` (8 arquivos) |
| 06 | [06-pista-sem-turno.md](06-pista-sem-turno.md) | `useShift` + `OpenShiftSheet` + PistaPage (sem turno) |
| 07 | [07-pista-com-turno.md](07-pista-com-turno.md) | Métricas + `NozzleList` + `FuelingForm` inline |
| 08 | [08-pista-fechar.md](08-pista-fechar.md) | `CloseShiftSheet` 2 etapas: encerrantes → reconciliação |
| 09 | [09-precos.md](09-precos.md) | `PriceTable` + `NewPriceForm` + `PriceHistoryModal` + PrecosPage |
| 10 | [10-cadastros-postos.md](10-cadastros-postos.md) | `CadastroSubnav` + `StatusToggle` + `StationSheet` + PostosPage |
| 11 | [11-cadastros-bombas-bicos.md](11-cadastros-bombas-bicos.md) | `PumpSheet` + BombasPage + `NozzleSheet` + BicosPage |
| 12 | [12-combustiveis.md](12-combustiveis.md) | CombustiveisPage (toggle inline) |
| 13 | [13-historico.md](13-historico.md) | `ShiftList` + `ShiftDetailModal` + HistoricoPage |

---

## Mapa de arquivos

### Novos
```
frontend/
├── tailwind.config.js
├── postcss.config.js
├── components.json            ← shadcn config
└── src/
    ├── index.css              ← @tailwind + CSS vars shadcn
    ├── types.ts               ← todos os tipos TS do backend
    ├── lib/
    │   ├── utils.ts           ← cn(), formatBRL(), formatLiters()
    │   ├── theme.ts           ← dark/light toggle + localStorage
    │   └── api-client.ts      ← fetch base + ApiError
    ├── context/
    │   └── StationContext.tsx
    ├── hooks/
    │   ├── useActiveStation.ts
    │   └── useShift.ts
    ├── api/
    │   ├── stations.ts
    │   ├── pumps.ts
    │   ├── nozzles.ts
    │   ├── fuels.ts
    │   ├── prices.ts
    │   ├── shifts.ts
    │   ├── readings.ts
    │   └── fuelings.ts
    ├── components/
    │   ├── layout/
    │   │   ├── AppShell.tsx
    │   │   ├── Sidebar.tsx
    │   │   └── TopBar.tsx
    │   ├── pista/
    │   │   ├── OpenShiftSheet.tsx
    │   │   ├── NozzleList.tsx
    │   │   ├── FuelingForm.tsx
    │   │   └── CloseShiftSheet.tsx
    │   ├── precos/
    │   │   ├── PriceTable.tsx
    │   │   ├── NewPriceForm.tsx
    │   │   └── PriceHistoryModal.tsx
    │   ├── cadastros/
    │   │   ├── CadastroSubnav.tsx
    │   │   ├── StatusToggle.tsx
    │   │   ├── StationSheet.tsx
    │   │   ├── PumpSheet.tsx
    │   │   └── NozzleSheet.tsx
    │   └── historico/
    │       ├── ShiftList.tsx
    │       └── ShiftDetailModal.tsx
    └── pages/
        ├── PistaPage.tsx
        ├── PrecosPage.tsx
        ├── CadastrosPage.tsx
        ├── PostosPage.tsx
        ├── BombasPage.tsx
        ├── BicosPage.tsx
        ├── CombustiveisPage.tsx
        └── HistoricoPage.tsx
```

### Modificados
- `frontend/vite.config.ts` — proxy + `@/` alias
- `frontend/tsconfig.json` — `paths: { "@/*": ["./src/*"] }`
- `frontend/src/App.tsx` — BrowserRouter + StationProvider + rotas
- `frontend/src/main.tsx` — add `initTheme()`

---

## Tipos principais (derivados do backend)

```typescript
// StationResponse
type Station = { id: string; name: string; cnpj: string; address: string; city: string; state: string; active: boolean }

// PumpResponse
type Pump = { id: string; stationId: string; number: number; status: 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE' }

// NozzleResponse  ← sem fuelName, sem pumpNumber
type Nozzle = { id: string; pumpId: string; fuelId: string; number: number; active: boolean }

// FuelResponse
type Fuel = { id: string; name: string; unit: string; active: boolean }

// FuelPriceResponse  ← campo: effectiveFrom (não validFrom)
type FuelPrice = { id: string; fuelId: string; fuelName: string; price: number; effectiveFrom: string }

// ShiftResponse  ← sem totalVolume/totalRevenue (métricas vêm de ShiftSummaryResponse)
type Shift = { id: string; stationId: string; stationName: string; employeeName: string; status: 'OPEN' | 'CLOSED'; openedAt: string; closedAt: string | null; notes: string | null }

// ShiftSummaryResponse  ← de GET /api/shifts/{id}/fuelings
type ShiftSummary = { shiftId: string; fuelings: Fueling[]; totalLiters: number; totalAmount: number }

// FuelingResponse  ← sem cancelled, sem shiftId
type Fueling = { id: string; nozzleId: string; nozzleNumber: number; fuelName: string; liters: number; unitPrice: number; totalAmount: number; paymentMethod: string; vehiclePlate: string | null; fueledAt: string }

// ReconciliationLineResponse
type ReconciliationLine = { nozzleId: string; nozzleNumber: number; fuelName: string; openingTotalizer: number; closingTotalizer: number; measuredLiters: number; fueledLiters: number; divergenceLiters: number }

// ShiftReconciliationResponse  ← sem totalRevenue
type ShiftReconciliation = { shiftId: string; lines: ReconciliationLine[]; totalMeasuredLiters: number; totalFueledLiters: number; totalDivergenceLiters: number }

// PageResponse<T>  ← campo: page (não number)
type Page<T> = { content: T[]; page: number; size: number; totalElements: number; totalPages: number }
```

## Requests

```typescript
// POST /api/shifts
type OpenShiftRequest = { stationId: string; employeeName: string; notes?: string }

// POST /api/shifts/{id}/readings
type RegisterReadingRequest = { nozzleId: string; type: 'OPENING' | 'CLOSING'; totalizer: number }

// POST /api/shifts/{id}/fuelings
type RegisterFuelingRequest = { nozzleId: string; liters: number; totalAmount: number; paymentMethod: string; vehiclePlate?: string; notes?: string }
```

## PaymentMethod enum (backend)
```
CASH | CREDIT_CARD | DEBIT_CARD | PIX | FLEET | VOUCHER
```

## Rota de abertura para detecção de turno aberto
```
GET /api/stations/{id}/shifts?status=OPEN&size=1
→ Page<Shift>  →  content[0] ou null
```
