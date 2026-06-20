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

export function deletePump(id: string): Promise<void> {
  return api.delete<void>(`/pumps/${id}`)
}
