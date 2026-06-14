import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus, Pencil } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { TopBar } from '@/components/layout/TopBar'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { SpendProgress } from '@/components/frota/SpendProgress'
import { FleetClientSheet } from '@/components/frota/FleetClientSheet'
import { getFleetClients, patchFleetClientStatus } from '@/api/fleet-clients'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { FleetClient } from '@/types'

export function FrotaClientesPage() {
  const { station } = useActiveStation()
  const navigate = useNavigate()

  const { data: clients = [], isLoading } = useQuery({
    queryKey: ['fleet-clients', station?.id],
    queryFn: () => getFleetClients(station!.id),
    enabled: !!station,
  })

  const [sheetOpen, setSheetOpen] = useState(false)
  const [editClient, setEditClient] = useState<FleetClient | undefined>()

  function openCreate() {
    setEditClient(undefined)
    setSheetOpen(true)
  }

  function openEdit(client: FleetClient) {
    setEditClient(client)
    setSheetOpen(true)
  }

  if (!station) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Frota — Clientes" />
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-slate-400">Selecione um posto ativo na barra lateral.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Frota — Clientes" subtitle="Empresas cadastradas no controle de frota" />

      <div className="flex-1 overflow-auto p-6">
        <div className="mb-4 flex justify-end">
          <Button size="sm" className="bg-orange-600 hover:bg-orange-700" onClick={openCreate}>
            <Plus size={14} className="mr-1" />
            Novo cliente
          </Button>
        </div>

        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : clients.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum cliente de frota cadastrado.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Razão Social
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    CNPJ
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Limite / Consumo
                  </th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Status
                  </th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Ações
                  </th>
                </tr>
              </thead>
              <tbody>
                {clients.map((client) => (
                  <tr
                    key={client.id}
                    className="cursor-pointer border-b last:border-0 hover:bg-slate-50 dark:hover:bg-slate-800"
                    onClick={() => navigate(`/frota/clientes/${client.id}`)}
                  >
                    <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">
                      {client.companyName}
                      {client.tradeName && (
                        <span className="ml-1 text-xs font-normal text-slate-400">
                          ({client.tradeName})
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">{client.cnpj}</td>
                    <td className="px-4 py-3">
                      <SpendProgress spend={client.currentMonthSpend} limit={client.monthlyLimit} />
                    </td>
                    <td className="px-4 py-3 text-center" onClick={(e) => e.stopPropagation()}>
                      <StatusToggle
                        id={client.id}
                        active={client.active}
                        queryKeys={[['fleet-clients', station.id]]}
                        onToggle={(id, active) => patchFleetClientStatus(id, active)}
                      />
                    </td>
                    <td className="px-4 py-3 text-center" onClick={(e) => e.stopPropagation()}>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-7 w-7"
                        onClick={() => openEdit(client)}
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

      <FleetClientSheet
        open={sheetOpen}
        onOpenChange={setSheetOpen}
        client={editClient}
        stationId={station.id}
      />
    </div>
  )
}
