import { api } from '@/lib/api-client'

export interface CashRegisterData {
  id: string
  stationId: string
  status: 'OPEN' | 'CLOSED'
  openedAt: string
  closedAt: string | null
  openingBalance: number
  closingBalance: number | null
  notes: string | null
}

export interface CashMovementData {
  id: string
  type: 'INCOME' | 'EXPENSE'
  category: string
  description: string | null
  amount: number
  paymentMethod: string | null
  createdAt: string
}

export interface CashRegisterSummary {
  register: CashRegisterData
  movements: CashMovementData[]
  totalIncome: number
  totalExpense: number
  balance: number
}

export function openCashRegister(req: { stationId: string; openingBalance: number; notes?: string }): Promise<CashRegisterData> {
  return api.post<CashRegisterData>('/cash-registers', req)
}

export function closeCashRegister(id: string, closingBalance: number): Promise<CashRegisterData> {
  return api.post<CashRegisterData>(`/cash-registers/${id}/close`, { closingBalance })
}

export function addCashMovement(id: string, req: {
  type: string; category: string; description?: string; amount: number; paymentMethod?: string
}): Promise<CashMovementData> {
  return api.post<CashMovementData>(`/cash-registers/${id}/movements`, req)
}

export function getCashRegisterSummary(id: string): Promise<CashRegisterSummary> {
  return api.get<CashRegisterSummary>(`/cash-registers/${id}`)
}
