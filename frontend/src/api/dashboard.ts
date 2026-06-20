import { api } from '@/lib/api-client'

export interface DashboardData {
  totalRevenue: number
  totalLiters: number
  fuelingCount: number
  openServiceOrders: number
  fleetFuelingCount: number
  activeShift: { employeeName: string; openedAt: string } | null
  revenueByFuel: { fuelName: string; revenue: number; liters: number }[]
  revenueByPayment: { paymentMethod: string; revenue: number; count: number }[]
}

export function getDashboard(stationId: string): Promise<DashboardData> {
  return api.get<DashboardData>(`/dashboard?stationId=${stationId}`)
}
