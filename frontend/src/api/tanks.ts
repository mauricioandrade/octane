import { api } from '@/lib/api-client'
import type { Page } from '@/types'

export interface TankData {
  id: string
  stationId: string
  fuelId: string
  fuelName: string
  name: string
  capacity: number
  currentLevel: number
  minimumLevel: number
  active: boolean
  belowMinimum: boolean
}

export interface TankMovementData {
  id: string
  type: string
  volumeLiters: number
  previousLevel: number
  newLevel: number
  notes: string | null
  createdAt: string
}

export function getTanks(stationId: string): Promise<TankData[]> {
  return api.get<TankData[]>(`/tanks?stationId=${stationId}`)
}

export function createTank(req: {
  stationId: string; fuelId: string; name: string; capacity: number; minimumLevel: number
}): Promise<TankData> {
  return api.post<TankData>('/tanks', req)
}

export function registerDelivery(tankId: string, req: { volumeLiters: number; notes?: string }): Promise<TankData> {
  return api.post<TankData>(`/tanks/${tankId}/deliveries`, req)
}

export function adjustTankLevel(tankId: string, req: { newLevel: number; notes?: string }): Promise<TankData> {
  return api.post<TankData>(`/tanks/${tankId}/adjustments`, req)
}

export function getTankMovements(tankId: string, page = 0, size = 20): Promise<Page<TankMovementData>> {
  return api.get<Page<TankMovementData>>(`/tanks/${tankId}/movements?page=${page}&size=${size}`)
}
