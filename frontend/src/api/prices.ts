import { api } from '@/lib/api-client'
import type { FuelPrice, CreatePriceRequest } from '@/types'

export function getCurrentPrices(stationId: string): Promise<FuelPrice[]> {
  return api.get<FuelPrice[]>(`/stations/${stationId}/prices`)
}

export function getPriceHistory(stationId: string, fuelId: string): Promise<FuelPrice[]> {
  return api.get<FuelPrice[]>(`/stations/${stationId}/prices/history?fuelId=${fuelId}`)
}

export function createPrice(stationId: string, req: CreatePriceRequest): Promise<FuelPrice> {
  return api.post<FuelPrice>(`/stations/${stationId}/prices`, req)
}
