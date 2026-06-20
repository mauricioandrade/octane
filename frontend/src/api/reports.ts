import { api } from '@/lib/api-client'

export interface SalesReport {
  totalRevenue: number
  totalLiters: number
  totalCount: number
  daily: { date: string; revenue: number; liters: number; count: number }[]
  byFuel: { fuelName: string; revenue: number; liters: number; count: number }[]
  byPayment: { paymentMethod: string; revenue: number; count: number }[]
}

export interface ShiftReport {
  shifts: {
    employeeName: string
    openedAt: string
    closedAt: string
    durationMinutes: number
    revenue: number
    liters: number
    fuelingCount: number
  }[]
  totalRevenue: number
  totalLiters: number
  totalFuelings: number
}

export function getSalesReport(stationId: string, from: string, to: string): Promise<SalesReport> {
  return api.get<SalesReport>(`/reports/sales?stationId=${stationId}&from=${from}&to=${to}`)
}

export function getShiftReport(stationId: string, from: string, to: string): Promise<ShiftReport> {
  return api.get<ShiftReport>(`/reports/shifts?stationId=${stationId}&from=${from}&to=${to}`)
}
