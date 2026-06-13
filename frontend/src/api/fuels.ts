import { api } from '@/lib/api-client'
import type { Fuel } from '@/types'

export function getFuels(active?: boolean): Promise<Fuel[]> {
  const q = active !== undefined ? `?active=${active}` : ''
  return api.get<Fuel[]>(`/fuels${q}`)
}

export function patchFuelStatus(id: string, active: boolean): Promise<Fuel> {
  return api.patch<Fuel>(`/fuels/${id}/status`, { active })
}
