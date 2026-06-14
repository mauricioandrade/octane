import { api } from '@/lib/api-client'
import type {
  CommissionRule,
  CommissionEntry,
  CreateCommissionRuleRequest,
  UpdateCommissionRuleRequest,
} from '@/types'

export function createCommissionRule(req: CreateCommissionRuleRequest): Promise<CommissionRule> {
  return api.post<CommissionRule>('/commission/rules', req)
}

export function listCommissionRules(stationId: string, active?: boolean): Promise<CommissionRule[]> {
  const qs = new URLSearchParams({ stationId })
  if (active !== undefined) qs.set('active', String(active))
  return api.get<CommissionRule[]>(`/commission/rules?${qs}`)
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
  },
): Promise<CommissionEntry[]> {
  const qs = new URLSearchParams({ stationId })
  if (params?.paid !== undefined) qs.set('paid', String(params.paid))
  if (params?.from) qs.set('from', params.from)
  if (params?.to) qs.set('to', params.to)
  return api.get<CommissionEntry[]>(`/commission/entries?${qs}`)
}

export function getShiftCommissionEntry(shiftId: string): Promise<CommissionEntry | null> {
  return api.get<CommissionEntry>(`/commission/shifts/${shiftId}/entry`).catch(() => null)
}

export function markCommissionPaid(id: string): Promise<CommissionEntry> {
  return api.post<CommissionEntry>(`/commission/entries/${id}/pay`)
}
