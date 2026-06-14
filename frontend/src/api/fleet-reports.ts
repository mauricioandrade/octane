import { api } from '@/lib/api-client'
import type { FleetConsumptionReport } from '@/types'

export function getFleetConsumptionReport(params: {
  stationId: string
  clientId?: string
  vehicleId?: string
  driverId?: string
  from: string
  to: string
}): Promise<FleetConsumptionReport> {
  const entries = Object.entries(params).filter(([, v]) => v) as [string, string][]
  const q = new URLSearchParams(entries)
  return api.get<FleetConsumptionReport>(`/fleet/reports/consumption?${q}`)
}

export function downloadFleetCsv(params: {
  stationId: string
  clientId?: string
  from: string
  to: string
}): string {
  const entries = Object.entries(params).filter(([, v]) => v) as [string, string][]
  const q = new URLSearchParams(entries)
  return `/api/fleet/reports/consumption/csv?${q}`
}
