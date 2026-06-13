import { api } from '@/lib/api-client'
import type { Shift, OpenShiftRequest, Page } from '@/types'

export function openShift(req: OpenShiftRequest): Promise<Shift> {
  return api.post<Shift>('/shifts', req)
}

export function closeShift(id: string): Promise<Shift> {
  return api.post<Shift>(`/shifts/${id}/close`)
}

export function getShift(id: string): Promise<Shift> {
  return api.get<Shift>(`/shifts/${id}`)
}

export async function getOpenShift(stationId: string): Promise<Shift | null> {
  const page = await api.get<Page<Shift>>(
    `/stations/${stationId}/shifts?status=OPEN&size=1`,
  )
  return page.content[0] ?? null
}

export function listShifts(
  stationId: string,
  params: {
    page?: number
    size?: number
    status?: string
    from?: string
    to?: string
  } = {},
): Promise<Page<Shift>> {
  const q = new URLSearchParams()
  if (params.page !== undefined) q.set('page', String(params.page))
  if (params.size !== undefined) q.set('size', String(params.size))
  if (params.status) q.set('status', params.status)
  if (params.from) q.set('from', params.from)
  if (params.to) q.set('to', params.to)
  const qs = q.toString()
  return api.get<Page<Shift>>(`/stations/${stationId}/shifts${qs ? `?${qs}` : ''}`)
}

export function getReconciliation(shiftId: string) {
  return api.get<import('@/types').ShiftReconciliation>(`/shifts/${shiftId}/reconciliation`)
}
