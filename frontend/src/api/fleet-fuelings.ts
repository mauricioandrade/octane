import { api } from '@/lib/api-client'
import type { FleetFueling } from '@/types'

export function registerFleetFueling(req: {
  shiftId: string
  nozzleId: string
  liters?: number
  totalAmount?: number
  paymentMethod: string
  driverId: string
  vehicleId: string
  odometer: number
  notes?: string
}): Promise<FleetFueling> {
  return api.post<FleetFueling>('/fleet/fuelings', req)
}

export function getFleetFuelingsByClient(
  clientId: string,
  stationId: string,
  params?: {
    from?: string
    to?: string
    vehicleId?: string
    driverId?: string
  },
): Promise<FleetFueling[]> {
  const allParams = { stationId, ...(params ?? {}) }
  const entries = Object.entries(allParams).filter(([, v]) => v) as [string, string][]
  const q = entries.length ? '?' + new URLSearchParams(entries) : ''
  return api.get<FleetFueling[]>(`/fleet/clients/${clientId}/fuelings${q}`)
}
