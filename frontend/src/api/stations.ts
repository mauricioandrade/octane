import { api } from '@/lib/api-client'
import type { Station, CreateStationRequest } from '@/types'

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
