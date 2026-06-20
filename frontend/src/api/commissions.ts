import { api } from '@/lib/api-client'
import type {
  CommissionRule,
  CommissionEntry,
  CreateCommissionRuleRequest,
  UpdateCommissionRuleRequest,
  Page,
} from '@/types'

export function createCommissionRule(req: CreateCommissionRuleRequest): Promise<CommissionRule> {
  return api.post<CommissionRule>('/commission/rules', req)
}

export function listCommissionRules(
  stationId: string,
  active?: boolean,
  page = 0,
  size = 20,
): Promise<Page<CommissionRule>> {
  const qs = new URLSearchParams({ stationId, page: String(page), size: String(size) })
  if (active !== undefined) qs.set('active', String(active))
  return api.get<Page<CommissionRule>>(`/commission/rules?${qs}`)
}

export function updateCommissionRule(
  id: string,
  req: UpdateCommissionRuleRequest,
): Promise<CommissionRule> {
  return api.put<CommissionRule>(`/commission/rules/${id}`, req)
}

export function toggleCommissionRuleStatus(id: string, active: boolean): Promise<CommissionRule> {
  return api.patch<CommissionRule>(`/commission/rules/${id}/status`, { active })
}

export function listCommissionEntries(
  stationId: string,
  params?: {
    paid?: boolean
    from?: string
    to?: string
    page?: number
    size?: number
  },
): Promise<Page<CommissionEntry>> {
  const qs = new URLSearchParams({ stationId })
  if (params?.paid !== undefined) qs.set('paid', String(params.paid))
  if (params?.from) qs.set('from', params.from)
  if (params?.to) qs.set('to', params.to)
  qs.set('page', String(params?.page ?? 0))
  qs.set('size', String(params?.size ?? 20))
  return api.get<Page<CommissionEntry>>(`/commission/entries?${qs}`)
}

export async function getShiftCommissionEntry(shiftId: string): Promise<CommissionEntry | null> {
  const result = await api.get<CommissionEntry>(`/commission/shifts/${shiftId}/entry`)
  return result ?? null
}

export function markCommissionPaid(id: string): Promise<CommissionEntry> {
  return api.post<CommissionEntry>(`/commission/entries/${id}/pay`)
}

export function deleteCommissionRule(id: string): Promise<void> {
  return api.delete<void>(`/commission/rules/${id}`)
}
