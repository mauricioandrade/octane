import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, Pencil } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { TopBar } from '@/components/layout/TopBar'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { FleetVehicleSheet } from '@/components/frota/FleetVehicleSheet'
import { getFleetClients } from '@/api/fleet-clients'
import { getFleetVehiclesByClient, patchFleetVehicleStatus } from '@/api/fleet-vehicles'
import { useActiveStation } from '@/hooks/useActiveStation'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { FleetVehicle } from '@/types'

export function FrotaVeiculosPage() {
  const { station } = useActiveStation()
  const [selectedClientId, setSelectedClientId] = useState<string>('')
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editVehicle, setEditVehicle] = useState<FleetVehicle | undefined>()

  const { data: clients = [] } = useQuery({
    queryKey: ['fleet-clients', station?.id],
    queryFn: () => getFleetClients(station!.id),
    enabled: !!station,
  })

  const { data: vehicles = [], isLoading } = useQuery({
    queryKey: ['fleet-vehicles', selectedClientId],
    queryFn: () => getFleetVehiclesByClient(selectedClientId),
    enabled: !!selectedClientId,
  })

  if (!station) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Frota — Veículos" />
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-slate-400">Selecione um posto ativo na barra lateral.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Frota — Veículos" subtitle="Veículos cadastrados por cliente" />

      <div className="flex-1 overflow-auto p-6">
        <div className="mb-4 flex items-center gap-3">
          <div className="w-72">
            <Select value={selectedClientId} onValueChange={setSelectedClientId}>
              <SelectTrigger>
                <SelectValue placeholder="Selecionar cliente…" />
              </SelectTrigger>
              <SelectContent>
                {clients.map((c) => (
                  <SelectItem key={c.id} value={c.id}>
                    {c.companyName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <Button
            size="sm"
            className="ml-auto bg-orange-600 hover:bg-orange-700"
            disabled={!selectedClientId}
            onClick={() => { setEditVehicle(undefined); setSheetOpen(true) }}
          >
            <Plus size={14} className="mr-1" />
            Novo veículo
          </Button>
        </div>

        {!selectedClientId ? (
          <p className="text-sm text-slate-400">Selecione um cliente para ver os veículos.</p>
        ) : isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : vehicles.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum veículo cadastrado para este cliente.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Placa</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Modelo</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Combustível Permitido</th>
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
                        queryKeys={[['fleet-vehicles', selectedClientId]]}
                        onToggle={(id, active) => patchFleetVehicleStatus(id, active)}
                      />
                    </td>
                    <td className="px-4 py-3 text-center">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-7 w-7"
                        onClick={() => { setEditVehicle(v); setSheetOpen(true) }}
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
      </div>

      <FleetVehicleSheet
        open={sheetOpen}
        onOpenChange={setSheetOpen}
        vehicle={editVehicle}
        clientId={selectedClientId}
      />
    </div>
  )
}
