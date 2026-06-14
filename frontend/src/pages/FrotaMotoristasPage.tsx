import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, Pencil } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { TopBar } from '@/components/layout/TopBar'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { FleetDriverSheet } from '@/components/frota/FleetDriverSheet'
import { getFleetClients } from '@/api/fleet-clients'
import { getFleetDriversByClient, patchFleetDriverStatus } from '@/api/fleet-drivers'
import { useActiveStation } from '@/hooks/useActiveStation'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import type { FleetDriver } from '@/types'

export function FrotaMotoristasPage() {
  const { station } = useActiveStation()
  const [selectedClientId, setSelectedClientId] = useState<string>('')
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editDriver, setEditDriver] = useState<FleetDriver | undefined>()

  const { data: clients = [] } = useQuery({
    queryKey: ['fleet-clients', station?.id],
    queryFn: () => getFleetClients(station!.id),
    enabled: !!station,
  })

  const { data: drivers = [], isLoading } = useQuery({
    queryKey: ['fleet-drivers', selectedClientId],
    queryFn: () => getFleetDriversByClient(selectedClientId),
    enabled: !!selectedClientId,
  })

  if (!station) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Frota — Motoristas" />
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-slate-400">Selecione um posto ativo na barra lateral.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Frota — Motoristas" subtitle="Motoristas cadastrados por cliente" />

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
            onClick={() => { setEditDriver(undefined); setSheetOpen(true) }}
          >
            <Plus size={14} className="mr-1" />
            Novo motorista
          </Button>
        </div>

        {!selectedClientId ? (
          <p className="text-sm text-slate-400">Selecione um cliente para ver os motoristas.</p>
        ) : isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : drivers.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum motorista cadastrado para este cliente.</p>
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
                        queryKeys={[['fleet-drivers', selectedClientId]]}
                        onToggle={(id, active) => patchFleetDriverStatus(id, active)}
                      />
                    </td>
                    <td className="px-4 py-3 text-center">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-7 w-7"
                        onClick={() => { setEditDriver(d); setSheetOpen(true) }}
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

      <FleetDriverSheet
        open={sheetOpen}
        onOpenChange={setSheetOpen}
        driver={editDriver}
        clientId={selectedClientId}
      />
    </div>
  )
}
