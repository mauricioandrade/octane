import { api } from '@/lib/api-client'
import type { RegisterReadingRequest } from '@/types'

export function registerReading(shiftId: string, req: RegisterReadingRequest): Promise<void> {
  return api.post<void>(`/shifts/${shiftId}/readings`, req)
}
