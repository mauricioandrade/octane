# Task 05 — API layer: todos os src/api/*.ts

**Files:**
- Create: `frontend/src/api/stations.ts`
- Create: `frontend/src/api/pumps.ts`
- Create: `frontend/src/api/nozzles.ts`
- Create: `frontend/src/api/fuels.ts`
- Create: `frontend/src/api/prices.ts`
- Create: `frontend/src/api/shifts.ts`
- Create: `frontend/src/api/readings.ts`
- Create: `frontend/src/api/fuelings.ts`

---

- [ ] **Step 1: Criar `frontend/src/api/stations.ts`**

```typescript
import { api } from '@/lib/api-client'
import type { Station, CreateStationRequest, Page } from '@/types'

export function getStations(active?: boolean): Promise<Station[]> {
  const q = active !== undefined ? `?active=${active}` : ''
  return api.get<Station[]>(`/stations${q}`)
}

export function getStation(id: string): Promise<Station> {
  return api.get<Station>(`/stations/${id}`)
}

export function createStation(req: CreateStationRequest): Promise<Station> {
  return api.post<Station>('/stations', req)
}

export function updateStation(id: string, req: CreateStationRequest): Promise<Station> {
  return api.put<Station>(`/stations/${id}`, req)
}

export function patchStationStatus(id: string, active: boolean): Promise<Station> {
  return api.patch<Station>(`/stations/${id}/status`, { active })
}
```

- [ ] **Step 2: Criar `frontend/src/api/pumps.ts`**

```typescript
import { api } from '@/lib/api-client'
import type { Pump, CreatePumpRequest, PumpStatus } from '@/types'

export function getPumps(stationId: string, status?: PumpStatus): Promise<Pump[]> {
  const q = status ? `?status=${status}` : ''
  return api.get<Pump[]>(`/stations/${stationId}/pumps${q}`)
}

export function createPump(stationId: string, req: CreatePumpRequest): Promise<Pump> {
  return api.post<Pump>(`/stations/${stationId}/pumps`, req)
}

export function updatePump(id: string, req: CreatePumpRequest): Promise<Pump> {
  return api.put<Pump>(`/pumps/${id}`, req)
}

export function patchPumpStatus(id: string, status: PumpStatus): Promise<Pump> {
  return api.patch<Pump>(`/pumps/${id}/status`, { status })
}
```

- [ ] **Step 3: Criar `frontend/src/api/nozzles.ts`**

```typescript
import { api } from '@/lib/api-client'
import type { Nozzle, CreateNozzleRequest } from '@/types'

export function getNozzles(pumpId: string, active?: boolean): Promise<Nozzle[]> {
  const q = active !== undefined ? `?active=${active}` : ''
  return api.get<Nozzle[]>(`/pumps/${pumpId}/nozzles${q}`)
}

export function createNozzle(pumpId: string, req: CreateNozzleRequest): Promise<Nozzle> {
  return api.post<Nozzle>(`/pumps/${pumpId}/nozzles`, req)
}

export function updateNozzle(id: string, req: CreateNozzleRequest): Promise<Nozzle> {
  return api.put<Nozzle>(`/nozzles/${id}`, req)
}

export function patchNozzleStatus(id: string, active: boolean): Promise<Nozzle> {
  return api.patch<Nozzle>(`/nozzles/${id}/status`, { active })
}
```

- [ ] **Step 4: Criar `frontend/src/api/fuels.ts`**

```typescript
import { api } from '@/lib/api-client'
import type { Fuel } from '@/types'

export function getFuels(active?: boolean): Promise<Fuel[]> {
  const q = active !== undefined ? `?active=${active}` : ''
  return api.get<Fuel[]>(`/fuels${q}`)
}

export function patchFuelStatus(id: string, active: boolean): Promise<Fuel> {
  return api.patch<Fuel>(`/fuels/${id}/status`, { active })
}
```

- [ ] **Step 5: Criar `frontend/src/api/prices.ts`**

```typescript
import { api } from '@/lib/api-client'
import type { FuelPrice, CreatePriceRequest } from '@/types'

export function getCurrentPrices(stationId: string): Promise<FuelPrice[]> {
  return api.get<FuelPrice[]>(`/stations/${stationId}/prices`)
}

export function getPriceHistory(stationId: string, fuelId: string): Promise<FuelPrice[]> {
  return api.get<FuelPrice[]>(`/stations/${stationId}/prices/history?fuelId=${fuelId}`)
}

export function createPrice(stationId: string, req: CreatePriceRequest): Promise<FuelPrice> {
  return api.post<FuelPrice>(`/stations/${stationId}/prices`, req)
}
```

- [ ] **Step 6: Criar `frontend/src/api/shifts.ts`**

```typescript
import { api } from '@/lib/api-client'
import type { Shift, OpenShiftRequest, Page } from '@/types'

export function openShift(req: OpenShiftRequest): Promise<Shift> {
  return api.post<Shift>('/shifts', req)
}

export function closeShift(id: string): Promise<Shift> {
  return api.post<Shift>(`/shifts/${id}/close`)
}

export function getShift(id: string): Promise<Shift> {
  return api.get<Shift>(`/shifts/${id}`)
}

export async function getOpenShift(stationId: string): Promise<Shift | null> {
  const page = await api.get<Page<Shift>>(
    `/stations/${stationId}/shifts?status=OPEN&size=1`,
  )
  return page.content[0] ?? null
}

export function listShifts(
  stationId: string,
  params: {
    page?: number
    size?: number
    status?: string
    from?: string
    to?: string
  } = {},
): Promise<Page<Shift>> {
  const q = new URLSearchParams()
  if (params.page !== undefined) q.set('page', String(params.page))
  if (params.size !== undefined) q.set('size', String(params.size))
  if (params.status) q.set('status', params.status)
  if (params.from) q.set('from', params.from)
  if (params.to) q.set('to', params.to)
  const qs = q.toString()
  return api.get<Page<Shift>>(`/stations/${stationId}/shifts${qs ? `?${qs}` : ''}`)
}

export function getReconciliation(shiftId: string) {
  return api.get<import('@/types').ShiftReconciliation>(`/shifts/${shiftId}/reconciliation`)
}
```

- [ ] **Step 7: Criar `frontend/src/api/readings.ts`**

```typescript
import { api } from '@/lib/api-client'
import type { RegisterReadingRequest } from '@/types'

export function registerReading(shiftId: string, req: RegisterReadingRequest): Promise<void> {
  return api.post<void>(`/shifts/${shiftId}/readings`, req)
}
```

- [ ] **Step 8: Criar `frontend/src/api/fuelings.ts`**

```typescript
import { api } from '@/lib/api-client'
import type { Fueling, ShiftSummary, RegisterFuelingRequest } from '@/types'

export function getShiftSummary(shiftId: string): Promise<ShiftSummary> {
  return api.get<ShiftSummary>(`/shifts/${shiftId}/fuelings`)
}

export function registerFueling(shiftId: string, req: RegisterFuelingRequest): Promise<Fueling> {
  return api.post<Fueling>(`/shifts/${shiftId}/fuelings`, req)
}

export function cancelFueling(shiftId: string, fuelingId: string): Promise<Fueling> {
  return api.post<Fueling>(`/shifts/${shiftId}/fuelings/${fuelingId}/cancel`)
}
```

- [ ] **Step 9: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros TypeScript. Se aparecer algum erro de tipo, verifique que os tipos em `src/types.ts` batem com os campos usados aqui.

- [ ] **Step 10: Commit**

```bash
cd ..
git add frontend/src/api/
git commit -m "feat(frontend): camada de API (stations, pumps, nozzles, fuels, prices, shifts, readings, fuelings)"
```
