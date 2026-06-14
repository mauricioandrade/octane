import { api } from '@/lib/api-client'
import type { FleetClient } from '@/types'

export function getFleetClients(stationId: string, active?: boolean): Promise<FleetClient[]> {
  const params = new URLSearchParams({ stationId })
  if (active !== undefined) params.set('active', String(active))
  return api.get<FleetClient[]>(`/fleet/clients?${params}`)
}

export function getFleetClient(id: string): Promise<FleetClient> {
  return api.get<FleetClient>(`/fleet/clients/${id}`)
}

export function createFleetClient(req: {
  stationId: string
  cnpj: string
  companyName: string
  tradeName?: string
  monthlyLimit?: number
}): Promise<FleetClient> {
  return api.post<FleetClient>('/fleet/clients', req)
}

export function updateFleetClient(
  id: string,
  req: {
    companyName: string
    tradeName?: string
    monthlyLimit?: number
    active: boolean
  },
): Promise<FleetClient> {
  return api.put<FleetClient>(`/fleet/clients/${id}`, req)
}

export function patchFleetClientStatus(id: string, active: boolean): Promise<FleetClient> {
  return api.patch<FleetClient>(`/fleet/clients/${id}/status`, { active })
}
