import { api } from '@/lib/api-client'
import type { ServiceOrder, CreateServiceOrderRequest, AddServiceOrderItemRequest } from '@/types'

export function createServiceOrder(req: CreateServiceOrderRequest): Promise<ServiceOrder> {
  return api.post<ServiceOrder>('/service-orders', req)
}

export function getServiceOrder(id: string): Promise<ServiceOrder> {
  return api.get<ServiceOrder>(`/service-orders/${id}`)
}

export function addServiceOrderItem(id: string, req: AddServiceOrderItemRequest): Promise<ServiceOrder> {
  return api.post<ServiceOrder>(`/service-orders/${id}/items`, req)
}

export function closeServiceOrder(id: string): Promise<ServiceOrder> {
  return api.post<ServiceOrder>(`/service-orders/${id}/close`)
}

export function cancelServiceOrder(id: string): Promise<ServiceOrder> {
  return api.post<ServiceOrder>(`/service-orders/${id}/cancel`)
}

export function listServiceOrders(
  stationId: string,
  params?: {
    status?: string
    from?: string
    to?: string
  },
): Promise<ServiceOrder[]> {
  const qs = new URLSearchParams()
  if (params?.status) qs.set('status', params.status)
  if (params?.from) qs.set('from', params.from)
  if (params?.to) qs.set('to', params.to)
  const query = qs.toString()
  return api.get<ServiceOrder[]>(
    `/stations/${stationId}/service-orders${query ? `?${query}` : ''}`,
  )
}

export function getVehicleHistory(plate: string): Promise<ServiceOrder[]> {
  return api.get<ServiceOrder[]>(`/service-orders/by-plate/${encodeURIComponent(plate)}`)
}
