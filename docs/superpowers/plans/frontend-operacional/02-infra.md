# Task 02 — Infra: Vite proxy, path alias, api-client, types

**Files:**
- Modify: `frontend/vite.config.ts`
- Modify: `frontend/tsconfig.json`
- Create: `frontend/src/lib/api-client.ts`
- Create: `frontend/src/types.ts`

---

- [ ] **Step 1: Atualizar `frontend/vite.config.ts`**

```typescript
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
```

- [ ] **Step 2: Atualizar `frontend/tsconfig.json` com paths alias**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "module": "ESNext",
    "skipLibCheck": true,
    "moduleResolution": "bundler",
    "allowImportingTsExtensions": true,
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx",
    "strict": true,
    "noUnusedLocals": true,
    "noUnusedParameters": true,
    "noFallthroughCasesInSwitch": true,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

- [ ] **Step 3: Criar `frontend/src/lib/api-client.ts`**

```typescript
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })

  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new ApiError(res.status, text || res.statusText)
  }

  if (res.status === 204) return undefined as T
  return res.json() as Promise<T>
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'POST',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),
  put: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'PUT',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),
  patch: <T>(path: string, body?: unknown) =>
    request<T>(path, {
      method: 'PATCH',
      body: body !== undefined ? JSON.stringify(body) : undefined,
    }),
}
```

- [ ] **Step 4: Criar `frontend/src/types.ts`**

Todos os tipos derivados dos Java Records do backend:

```typescript
// ── Station ──────────────────────────────────────────────────────────────────

export type Station = {
  id: string
  name: string
  cnpj: string
  address: string
  city: string
  state: string
  active: boolean
}

// ── Pump ─────────────────────────────────────────────────────────────────────

export type PumpStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'

export type Pump = {
  id: string
  stationId: string
  number: number
  status: PumpStatus
}

// ── Nozzle ───────────────────────────────────────────────────────────────────
// NozzleResponse do backend não inclui fuelName nem pumpNumber.
// O frontend enriquece via join com Fuel e Pump quando necessário.

export type Nozzle = {
  id: string
  pumpId: string
  fuelId: string
  number: number
  active: boolean
}

export type EnrichedNozzle = Nozzle & {
  fuelName: string
  pumpNumber: number
}

// ── Fuel ─────────────────────────────────────────────────────────────────────

export type Fuel = {
  id: string
  name: string
  unit: string
  active: boolean
}

// ── Price ─────────────────────────────────────────────────────────────────────
// Campo: effectiveFrom (não validFrom)

export type FuelPrice = {
  id: string
  fuelId: string
  fuelName: string
  price: number
  effectiveFrom: string
}

// ── Shift ─────────────────────────────────────────────────────────────────────
// ShiftResponse não tem totalVolume/totalRevenue.
// Métricas do turno vêm de ShiftSummaryResponse (GET /api/shifts/{id}/fuelings).

export type ShiftStatus = 'OPEN' | 'CLOSED'

export type Shift = {
  id: string
  stationId: string
  stationName: string
  employeeName: string
  status: ShiftStatus
  openedAt: string
  closedAt: string | null
  notes: string | null
}

// ── Fueling ──────────────────────────────────────────────────────────────────
// FuelingResponse não tem cancelled nem shiftId.

export type PaymentMethod = 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'PIX' | 'FLEET' | 'VOUCHER'

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  CASH: 'Dinheiro',
  CREDIT_CARD: 'Crédito',
  DEBIT_CARD: 'Débito',
  PIX: 'PIX',
  FLEET: 'Frota',
  VOUCHER: 'Voucher',
}

export type Fueling = {
  id: string
  nozzleId: string
  nozzleNumber: number
  fuelName: string
  liters: number
  unitPrice: number
  totalAmount: number
  paymentMethod: string
  vehiclePlate: string | null
  fueledAt: string
}

// ShiftSummaryResponse — de GET /api/shifts/{id}/fuelings
export type ShiftSummary = {
  shiftId: string
  fuelings: Fueling[]
  totalLiters: number
  totalAmount: number
}

// ── Reconciliation ───────────────────────────────────────────────────────────
// ShiftReconciliationResponse não tem totalRevenue.

export type ReconciliationLine = {
  nozzleId: string
  nozzleNumber: number
  fuelName: string
  openingTotalizer: number
  closingTotalizer: number
  measuredLiters: number
  fueledLiters: number
  divergenceLiters: number
}

export type ShiftReconciliation = {
  shiftId: string
  lines: ReconciliationLine[]
  totalMeasuredLiters: number
  totalFueledLiters: number
  totalDivergenceLiters: number
}

// ── Pagination ────────────────────────────────────────────────────────────────
// PageResponse do backend: campo page (não number)

export type Page<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// ── Requests ──────────────────────────────────────────────────────────────────

export type OpenShiftRequest = {
  stationId: string
  employeeName: string
  notes?: string
}

export type RegisterReadingRequest = {
  nozzleId: string
  type: 'OPENING' | 'CLOSING'
  totalizer: number
}

export type RegisterFuelingRequest = {
  nozzleId: string
  liters: number
  totalAmount: number
  paymentMethod: string
  vehiclePlate?: string
  notes?: string
}

export type CreateStationRequest = {
  name: string
  cnpj: string
  address: string
  city: string
  state: string
}

export type CreatePumpRequest = {
  number: number
}

export type CreateNozzleRequest = {
  number: number
  pumpId: string
  fuelId: string
}

export type CreatePriceRequest = {
  fuelId: string
  price: number
}
```

- [ ] **Step 5: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros. Se aparecer erro de `path` não encontrado, confirme que `@types/node` foi instalado no Step 1 da Task 01.

- [ ] **Step 6: Commit**

```bash
cd ..
git add frontend/vite.config.ts frontend/tsconfig.json frontend/src/lib/api-client.ts frontend/src/types.ts
git commit -m "feat(frontend): vite proxy, path alias, api-client e tipos"
```
