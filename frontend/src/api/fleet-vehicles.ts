import { api } from '@/lib/api-client'
import type { FleetVehicle } from '@/types'

export function getFleetVehiclesByClient(clientId: string, active?: boolean): Promise<FleetVehicle[]> {
  const params = active !== undefined ? `?active=${active}` : ''
  return api.get<FleetVehicle[]>(`/fleet/clients/${clientId}/vehicles${params}`)
}

export function getFleetVehicle(id: string): Promise<FleetVehicle> {
  return api.get<FleetVehicle>(`/fleet/vehicles/${id}`)
}

export function createFleetVehicle(req: {
  clientId: string
  plate: string
  model?: string
  allowedFuelId: string
}): Promise<FleetVehicle> {
  return api.post<FleetVehicle>('/fleet/vehicles', req)
}

export function updateFleetVehicle(
  id: string,
  req: {
    model?: string
    allowedFuelId: string
    active: boolean
  },
): Promise<FleetVehicle> {
  return api.put<FleetVehicle>(`/fleet/vehicles/${id}`, req)
}

export function patchFleetVehicleStatus(id: string, active: boolean): Promise<FleetVehicle> {
  return api.patch<FleetVehicle>(`/fleet/vehicles/${id}/status`, { active })
}
