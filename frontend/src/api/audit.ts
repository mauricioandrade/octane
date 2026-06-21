import { api } from '@/lib/api-client'
import type { Page } from '@/types'

export interface AuditLog {
  id: string
  username: string
  action: string
  entityType: string
  entityId: string | null
  details: string | null
  createdAt: string
}

export function listAuditLogs(page: number, size: number): Promise<Page<AuditLog>> {
  return api.get<Page<AuditLog>>(`/audit?page=${page}&size=${size}`)
}
