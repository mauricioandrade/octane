import { api } from '@/lib/api-client'
import type { Fueling, ShiftSummary, RegisterFuelingRequest } from '@/types'

export function getShiftSummary(shiftId: string): Promise<ShiftSummary> {
  return api.get<ShiftSummary>(`/shifts/${shiftId}/fuelings`)
}

export function registerFueling(shiftId: string, req: RegisterFuelingRequest): Promise<Fueling> {
  return api.post<Fueling>(`/shifts/${shiftId}/fuelings`, req)
}

export function cancelFueling(shiftId: string, fuelingId: string): Promise<Fueling> {
  return api.post<Fueling>(`/shifts/${shiftId}/fuelings/${fuelingId}/cancel`)
}
