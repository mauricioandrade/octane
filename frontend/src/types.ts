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
