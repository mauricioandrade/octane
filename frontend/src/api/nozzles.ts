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
