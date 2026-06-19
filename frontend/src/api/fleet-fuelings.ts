import { api } from '@/lib/api-client'
import type { FleetFueling, Page } from '@/types'

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
    page?: number
    size?: number
  },
): Promise<Page<FleetFueling>> {
  const allParams: Record<string, string> = {
    stationId,
    page: String(params?.page ?? 0),
    size: String(params?.size ?? 20),
  }
  if (params?.from) allParams.from = params.from
  if (params?.to) allParams.to = params.to
  if (params?.vehicleId) allParams.vehicleId = params.vehicleId
  if (params?.driverId) allParams.driverId = params.driverId
  const q = '?' + new URLSearchParams(allParams)
  return api.get<Page<FleetFueling>>(`/fleet/clients/${clientId}/fuelings${q}`)
}
