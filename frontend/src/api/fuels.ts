import { api } from '@/lib/api-client'
import type { Fuel } from '@/types'

export function getFuels(active?: boolean): Promise<Fuel[]> {
  const q = active !== undefined ? `?active=${active}` : ''
  return api.get<Fuel[]>(`/fuels${q}`)
}

export function patchFuelStatus(id: string, active: boolean): Promise<Fuel> {
  return api.patch<Fuel>(`/fuels/${id}/status`, { active })
}

export function createFuel(req: { name: string; unit: string }): Promise<Fuel> {
  return api.post<Fuel>('/fuels', req)
}

export function updateFuel(id: string, req: { name: string; unit: string }): Promise<Fuel> {
  return api.put<Fuel>(`/fuels/${id}`, req)
}

export function deleteFuel(id: string): Promise<void> {
  return api.delete<void>(`/fuels/${id}`)
}
