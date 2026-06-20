// ── User ─────────────────────────────────────────────────────────────────────

export type UserRole = 'ADMIN' | 'MANAGER' | 'ATTENDANT'

export const USER_ROLE_LABELS: Record<UserRole, string> = {
  ADMIN: 'Administrador',
  MANAGER: 'Gerente',
  ATTENDANT: 'Frentista',
}

export interface AppUser {
  id: string
  username: string
  name: string
  role: UserRole
  active: boolean
  createdAt: string
  updatedAt: string
}

export type CreateUserRequest = {
  username: string
  password: string
  name: string
  role: UserRole
}

export type UpdateUserRequest = {
  name?: string
  role?: UserRole
  active?: boolean
  password?: string
}

// ── Station ──────────────────────────────────────────────────────────────────

export type Station = {
  id: string
  name: string
  cnpj: string
  address: string
  city: string
  state: string
  active: boolean
}

// ── Pump ─────────────────────────────────────────────────────────────────────

export type PumpStatus = 'ACTIVE' | 'INACTIVE' | 'MAINTENANCE'

export type Pump = {
  id: string
  stationId: string
  number: number
  status: PumpStatus
}

// ── Nozzle ───────────────────────────────────────────────────────────────────
// NozzleResponse do backend não inclui fuelName nem pumpNumber.
// O frontend enriquece via join com Fuel e Pump quando necessário.

export type Nozzle = {
  id: string
  pumpId: string
  fuelId: string
  number: number
  active: boolean
}

export type EnrichedNozzle = Nozzle & {
  fuelName: string
  pumpNumber: number
}

// ── Fuel ─────────────────────────────────────────────────────────────────────

export type Fuel = {
  id: string
  name: string
  unit: string
  active: boolean
}

// ── Price ─────────────────────────────────────────────────────────────────────
// Campo: effectiveFrom (não validFrom)

export type FuelPrice = {
  id: string
  fuelId: string
  fuelName: string
  price: number
  effectiveFrom: string
}

// ── Shift ─────────────────────────────────────────────────────────────────────
// ShiftResponse não tem totalVolume/totalRevenue.
// Métricas do turno vêm de ShiftSummaryResponse (GET /api/shifts/{id}/fuelings).

export type ShiftStatus = 'OPEN' | 'CLOSED'

export type Shift = {
  id: string
  stationId: string
  stationName: string
  employeeName: string
  status: ShiftStatus
  openedAt: string
  closedAt: string | null
  notes: string | null
}

// ── Fueling ──────────────────────────────────────────────────────────────────
// FuelingResponse não tem cancelled nem shiftId.

export type PaymentMethod = 'CASH' | 'CREDIT_CARD' | 'DEBIT_CARD' | 'PIX' | 'FLEET' | 'VOUCHER'

export const PAYMENT_METHOD_LABELS: Record<PaymentMethod, string> = {
  CASH: 'Dinheiro',
  CREDIT_CARD: 'Crédito',
  DEBIT_CARD: 'Débito',
  PIX: 'PIX',
  FLEET: 'Frota',
  VOUCHER: 'Voucher',
}

export type Fueling = {
  id: string
  nozzleId: string
  nozzleNumber: number
  fuelName: string
  liters: number
  unitPrice: number
  totalAmount: number
  paymentMethod: string
  vehiclePlate: string | null
  fueledAt: string
}

// ShiftSummaryResponse — de GET /api/shifts/{id}/fuelings
export type ShiftSummary = {
  shiftId: string
  fuelings: Fueling[]
  totalLiters: number
  totalAmount: number
}

// ── Reconciliation ───────────────────────────────────────────────────────────
// ShiftReconciliationResponse não tem totalRevenue.

export type ReconciliationLine = {
  nozzleId: string
  nozzleNumber: number
  fuelName: string
  openingTotalizer: number
  closingTotalizer: number
  measuredLiters: number
  fueledLiters: number
  divergenceLiters: number
}

export type ShiftReconciliation = {
  shiftId: string
  lines: ReconciliationLine[]
  totalMeasuredLiters: number
  totalFueledLiters: number
  totalDivergenceLiters: number
}

// ── Pagination ────────────────────────────────────────────────────────────────
// PageResponse do backend: campo page (não number)

export type Page<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

// ── Requests ──────────────────────────────────────────────────────────────────

export type OpenShiftRequest = {
  stationId: string
  employeeName: string
  notes?: string
}

export type RegisterReadingRequest = {
  nozzleId: string
  type: 'OPENING' | 'CLOSING'
  totalizer: number
}

export type RegisterFuelingRequest = {
  nozzleId: string
  liters: number
  totalAmount: number
  paymentMethod: string
  vehiclePlate?: string
  notes?: string
}

export type CreateStationRequest = {
  name: string
  cnpj: string
  address: string
  city: string
  state: string
}

export type CreatePumpRequest = {
  number: number
}

export type CreateNozzleRequest = {
  number: number
  pumpId: string
  fuelId: string
}

export type CreatePriceRequest = {
  fuelId: string
  price: number
}

// ── Fleet ─────────────────────────────────────────────────────────────────────

export interface FleetClient {
  id: string
  stationId: string
  cnpj: string
  companyName: string
  tradeName?: string
  monthlyLimit?: number
  currentMonthSpend: number
  active: boolean
  createdAt: string
}

export interface FleetVehicle {
  id: string
  clientId: string
  plate: string
  model?: string
  allowedFuelId: string
  allowedFuelName: string
  active: boolean
  createdAt: string
}

export interface FleetDriver {
  id: string
  clientId: string
  name: string
  cpf: string
  hasPIN: boolean
  hasRFID: boolean
  active: boolean
  createdAt: string
}

export interface FleetFueling {
  id: string
  fuelingId: string
  driver: FleetDriver
  vehicle: FleetVehicle
  liters: number
  unitPrice: number
  totalAmount: number
  paymentMethod: string
  odometer: number
  previousOdometer?: number
  odometerAlert: boolean
  fueledAt: string
}

export interface FleetDriverIdentification {
  driver: FleetDriver
  client: FleetClient
  vehicles: FleetVehicle[]
}

export interface FleetConsumptionReport {
  summary: {
    totalLiters: number
    totalAmount: number
    count: number
    odometerAlerts: number
  }
  lines: FleetConsumptionLine[]
}

export interface FleetConsumptionLine {
  fueledAt: string
  clientName: string
  clientCnpj: string
  driverName: string
  driverCpf: string
  vehiclePlate: string
  vehicleModel?: string
  fuelName: string
  liters: number
  totalAmount: number
  odometer: number
  odometerAlert: boolean
  paymentMethod: string
}

// ── Service Order ─────────────────────────────────────────────────────────────

export type ServiceOrderStatus = 'OPEN' | 'CLOSED' | 'CANCELLED'

export type ServiceOrderItem = {
  id: string
  description: string
  quantity: number
  unitPrice: number
  totalPrice: number
  createdAt: string
}

export type ServiceOrder = {
  id: string
  stationId: string
  stationName: string
  plate: string
  odometer: number
  customerName: string | null
  customerPhone: string | null
  status: ServiceOrderStatus
  notes: string | null
  items: ServiceOrderItem[]
  totalAmount: number
  openedAt: string
  closedAt: string | null
  cancelledAt: string | null
  createdAt: string
}

export type CreateServiceOrderRequest = {
  stationId: string
  plate: string
  odometer: number
  customerName?: string
  customerPhone?: string
  notes?: string
}

export type AddServiceOrderItemRequest = {
  description: string
  quantity: number
  unitPrice: number
}

// ── Comissão ──────────────────────────────────────────────────────────────────

export type CommissionRule = {
  id: string
  stationId: string
  employeeName: string
  rate: number          // ex: 0.0200 = 2%
  active: boolean
  createdAt: string
}

export type CommissionEntry = {
  id: string
  shiftId: string
  employeeName: string
  stationId: string
  stationName: string
  baseAmount: number
  rate: number
  commission: number
  paid: boolean
  paidAt: string | null
  createdAt: string
}

export type CreateCommissionRuleRequest = {
  stationId: string
  employeeName: string
  rate: number
}

export type UpdateCommissionRuleRequest = {
  employeeName?: string
  rate?: number
  active?: boolean
}
