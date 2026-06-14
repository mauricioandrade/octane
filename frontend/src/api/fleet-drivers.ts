import { api } from '@/lib/api-client'
import type { FleetDriver, FleetDriverIdentification } from '@/types'

export function getFleetDriversByClient(clientId: string, active?: boolean): Promise<FleetDriver[]> {
  const params = active !== undefined ? `?active=${active}` : ''
  return api.get<FleetDriver[]>(`/fleet/clients/${clientId}/drivers${params}`)
}

export function createFleetDriver(req: {
  clientId: string
  name: string
  cpf: string
  pin?: string
  rfidTag?: string
}): Promise<FleetDriver> {
  return api.post<FleetDriver>('/fleet/drivers', req)
}

export function updateFleetDriver(
  id: string,
  req: {
    name: string
    pin?: string
    rfidTag?: string
    active: boolean
  },
): Promise<FleetDriver> {
  return api.put<FleetDriver>(`/fleet/drivers/${id}`, req)
}

export function patchFleetDriverStatus(id: string, active: boolean): Promise<FleetDriver> {
  return api.patch<FleetDriver>(`/fleet/drivers/${id}/status`, { active })
}

export function identifyFleetDriver(req: {
  stationId: string
  cpf?: string
  pin?: string
  rfidTag?: string
  identifierType: 'CPF' | 'PIN' | 'RFID'
}): Promise<FleetDriverIdentification> {
  return api.post<FleetDriverIdentification>('/fleet/drivers/identify', req)
}
