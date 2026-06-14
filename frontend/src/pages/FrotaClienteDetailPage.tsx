import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Plus, Pencil } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { TopBar } from '@/components/layout/TopBar'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { SpendProgress } from '@/components/frota/SpendProgress'
import { OdometerAlert } from '@/components/frota/OdometerAlert'
import { FleetVehicleSheet } from '@/components/frota/FleetVehicleSheet'
import { FleetDriverSheet } from '@/components/frota/FleetDriverSheet'
import { getFleetClient, patchFleetClientStatus } from '@/api/fleet-clients'
import { getFleetVehiclesByClient, patchFleetVehicleStatus } from '@/api/fleet-vehicles'
import { getFleetDriversByClient, patchFleetDriverStatus } from '@/api/fleet-drivers'
import { getFleetFuelingsByClient } from '@/api/fleet-fuelings'
import type { FleetVehicle, FleetDriver } from '@/types'

type Tab = 'veiculos' | 'motoristas' | 'historico'

export function FrotaClienteDetailPage() {
  const { clientId } = useParams<{ clientId: string }>()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState<Tab>('veiculos')

  const [vehicleSheetOpen, setVehicleSheetOpen] = useState(false)
  const [editVehicle, setEditVehicle] = useState<FleetVehicle | undefined>()

  const [driverSheetOpen, setDriverSheetOpen] = useState(false)
  const [editDriver, setEditDriver] = useState<FleetDriver | undefined>()

  const { data: client, isLoading: clientLoading } = useQuery({
    queryKey: ['fleet-client', clientId],
    queryFn: () => getFleetClient(clientId!),
    enabled: !!clientId,
  })

  const { data: vehicles = [], isLoading: vehiclesLoading } = useQuery({
    queryKey: ['fleet-vehicles', clientId],
    queryFn: () => getFleetVehiclesByClient(clientId!),
    enabled: !!clientId,
  })

  const { data: drivers = [], isLoading: driversLoading } = useQuery({
    queryKey: ['fleet-drivers', clientId],
    queryFn: () => getFleetDriversByClient(clientId!),
    enabled: !!clientId,
  })

  const { data: fuelings = [], isLoading: fuelingsLoading } = useQuery({
    queryKey: ['fleet-fuelings', clientId],
    queryFn: () => getFleetFuelingsByClient(clientId!),
    enabled: !!clientId && activeTab === 'historico',
  })

  if (clientLoading) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Frota — Cliente" />
        <div className="flex-1 p-6">
          <Skeleton className="mb-4 h-16 w-full" />
          <Skeleton className="h-48 w-full" />
        </div>
      </div>
    )
  }

  if (!client) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Frota — Cliente" />
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-slate-400">Cliente não encontrado.</p>
        </div>
      </div>
    )
  }

  const tabClass = (t: Tab) =>
    `px-4 py-2 text-sm font-medium border-b-2 transition-colors ${
      activeTab === t
        ? 'border-orange-600 text-orange-600'
        : 'border-transparent text-slate-500 hover:text-slate-700 dark:text-slate-400 dark:hover:text-slate-200'
    }`

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title={client.companyName}
        subtitle={client.cnpj}
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/frota/clientes')}>
            <ArrowLeft size={14} className="mr-1" />
            Voltar
          </Button>
        }
      />

      <div className="flex-1 overflow-auto p-6">
        {/* Header card */}
        <div className="mb-6 flex flex-wrap items-center gap-4 rounded-lg border bg-white p-4 dark:bg-slate-900">
          <div className="flex-1">
            <h2 className="text-lg font-bold text-slate-800 dark:text-slate-100">
              {client.companyName}
            </h2>
            {client.tradeName && (
              <p className="text-sm text-slate-400">{client.tradeName}</p>
            )}
            <p className="text-xs text-slate-400">{client.cnpj}</p>
          </div>
          <div className="min-w-[160px]">
            <SpendProgress spend={client.currentMonthSpend} limit={client.monthlyLimit} />
          </div>
          <StatusToggle
            id={client.id}
            active={client.active}
            queryKeys={[['fleet-client', clientId!], ['fleet-clients']]}
            onToggle={(id, active) => patchFleetClientStatus(id, active)}
          />
        </div>

        {/* Tabs */}
        <div className="mb-4 flex gap-0 border-b">
          <button className={tabClass('veiculos')} onClick={() => setActiveTab('veiculos')}>
            Veículos
          </button>
          <button className={tabClass('motoristas')} onClick={() => setActiveTab('motoristas')}>
            Motoristas
          </button>
          <button className={tabClass('historico')} onClick={() => setActiveTab('historico')}>
            Histórico
          </button>
        </div>

        {/* Veículos */}
        {activeTab === 'veiculos' && (
          <>
            <div className="mb-3 flex justify-end">
              <Button
                size="sm"
                className="bg-orange-600 hover:bg-orange-700"
                onClick={() => { setEditVehicle(undefined); setVehicleSheetOpen(true) }}
              >
                <Plus size={14} className="mr-1" />
                Novo veículo
              </Button>
            </div>
            {vehiclesLoading ? (
              <div className="flex flex-col gap-2">
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
              </div>
            ) : vehicles.length === 0 ? (
              <p className="text-sm text-slate-400">Nenhum veículo cadastrado.</p>
            ) : (
              <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-slate-50 dark:bg-slate-800">
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Placa</th>
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Modelo</th>
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Combustível</th>
                      <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Status</th>
                      <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {vehicles.map((v) => (
                      <tr key={v.id} className="border-b last:border-0">
                        <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">{v.plate}</td>
                        <td className="px-4 py-3 text-slate-500">{v.model ?? '—'}</td>
                        <td className="px-4 py-3 text-slate-500">{v.allowedFuelName}</td>
                        <td className="px-4 py-3 text-center">
                          <StatusToggle
                            id={v.id}
                            active={v.active}
                            queryKeys={[['fleet-vehicles', clientId!]]}
                            onToggle={(id, active) => patchFleetVehicleStatus(id, active)}
                          />
                        </td>
                        <td className="px-4 py-3 text-center">
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-7 w-7"
                            onClick={() => { setEditVehicle(v); setVehicleSheetOpen(true) }}
                          >
                            <Pencil size={14} />
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}

        {/* Motoristas */}
        {activeTab === 'motoristas' && (
          <>
            <div className="mb-3 flex justify-end">
              <Button
                size="sm"
                className="bg-orange-600 hover:bg-orange-700"
                onClick={() => { setEditDriver(undefined); setDriverSheetOpen(true) }}
              >
                <Plus size={14} className="mr-1" />
                Novo motorista
              </Button>
            </div>
            {driversLoading ? (
              <div className="flex flex-col gap-2">
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
              </div>
            ) : drivers.length === 0 ? (
              <p className="text-sm text-slate-400">Nenhum motorista cadastrado.</p>
            ) : (
              <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-slate-50 dark:bg-slate-800">
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Nome</th>
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">CPF</th>
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Autenticação</th>
                      <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Status</th>
                      <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Ações</th>
                    </tr>
                  </thead>
                  <tbody>
                    {drivers.map((d) => (
                      <tr key={d.id} className="border-b last:border-0">
                        <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">{d.name}</td>
                        <td className="px-4 py-3 text-slate-500">{d.cpf}</td>
                        <td className="px-4 py-3">
                          <div className="flex gap-1">
                            {d.hasPIN && (
                              <Badge className="bg-blue-100 text-blue-700 text-xs">PIN</Badge>
                            )}
                            {d.hasRFID && (
                              <Badge className="bg-purple-100 text-purple-700 text-xs">RFID</Badge>
                            )}
                          </div>
                        </td>
                        <td className="px-4 py-3 text-center">
                          <StatusToggle
                            id={d.id}
                            active={d.active}
                            queryKeys={[['fleet-drivers', clientId!]]}
                            onToggle={(id, active) => patchFleetDriverStatus(id, active)}
                          />
                        </td>
                        <td className="px-4 py-3 text-center">
                          <Button
                            variant="ghost"
                            size="icon"
                            className="h-7 w-7"
                            onClick={() => { setEditDriver(d); setDriverSheetOpen(true) }}
                          >
                            <Pencil size={14} />
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}

        {/* Histórico */}
        {activeTab === 'historico' && (
          <>
            {fuelingsLoading ? (
              <div className="flex flex-col gap-2">
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
                <Skeleton className="h-10 w-full" />
              </div>
            ) : fuelings.length === 0 ? (
              <p className="text-sm text-slate-400">Nenhum abastecimento de frota registrado.</p>
            ) : (
              <div className="flex flex-col gap-2">
                {fuelings.map((f) => (
                  <div
                    key={f.id}
                    className="rounded-lg border bg-white p-4 dark:bg-slate-900"
                  >
                    <div className="flex flex-wrap items-center justify-between gap-2">
                      <div>
                        <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                          {f.driver.name} — {f.vehicle.plate}
                        </p>
                        <p className="text-xs text-slate-400">
                          {new Date(f.fueledAt).toLocaleString('pt-BR')}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                          R$ {f.totalAmount.toFixed(2)}
                        </p>
                        <p className="text-xs text-slate-400">{f.liters.toFixed(3)} L</p>
                      </div>
                    </div>
                    {f.odometerAlert && (
                      <div className="mt-2">
                        <OdometerAlert previous={f.previousOdometer} current={f.odometer} />
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>

      <FleetVehicleSheet
        open={vehicleSheetOpen}
        onOpenChange={setVehicleSheetOpen}
        vehicle={editVehicle}
        clientId={clientId!}
      />

      <FleetDriverSheet
        open={driverSheetOpen}
        onOpenChange={setDriverSheetOpen}
        driver={editDriver}
        clientId={clientId!}
      />
    </div>
  )
}
