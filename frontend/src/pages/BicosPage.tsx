import { useState } from 'react'
import { useQuery, useQueries } from '@tanstack/react-query'
import { Pencil, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { TopBar } from '@/components/layout/TopBar'
import { NozzleSheet } from '@/components/cadastros/NozzleSheet'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getPumps } from '@/api/pumps'
import { getNozzles, patchNozzleStatus } from '@/api/nozzles'
import { getFuels } from '@/api/fuels'
import type { Nozzle } from '@/types'

export function BicosPage() {
  const { station } = useActiveStation()
  const [filterPumpId, setFilterPumpId] = useState<string>('all')
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editNozzle, setEditNozzle] = useState<Nozzle | undefined>()

  const { data: pumps = [], isLoading: pumpsLoading } = useQuery({
    queryKey: ['pumps', station?.id],
    queryFn: () => getPumps(station!.id),
    enabled: !!station,
  })

  const nozzleResults = useQueries({
    queries: pumps.map((pump) => ({
      queryKey: ['nozzles', pump.id],
      queryFn: () => getNozzles(pump.id),
      enabled: pumps.length > 0,
    })),
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
  })

  const fuelById = Object.fromEntries(fuels.map((f) => [f.id, f]))
  const pumpById = Object.fromEntries(pumps.map((p) => [p.id, p]))

  const allNozzles = nozzleResults.flatMap((r) => r.data ?? [])
  const filteredNozzles =
    filterPumpId === 'all' ? allNozzles : allNozzles.filter((n) => n.pumpId === filterPumpId)

  function openCreate() {
    setEditNozzle(undefined)
    setSheetOpen(true)
  }

  function openEdit(nozzle: Nozzle) {
    setEditNozzle(nozzle)
    setSheetOpen(true)
  }

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center p-6 text-sm text-slate-400">
        Selecione um posto para gerenciar bicos.
      </div>
    )
  }

  const queryKeysForNozzle = (pumpId: string) => [['nozzles', pumpId]]

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title="Bicos"
        actions={
          <div className="flex items-center gap-2">
            <Select value={filterPumpId} onValueChange={setFilterPumpId}>
              <SelectTrigger className="h-8 w-36 text-xs">
                <SelectValue placeholder="Filtrar bomba" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todas as bombas</SelectItem>
                {pumps.map((p) => (
                  <SelectItem key={p.id} value={p.id}>
                    Bomba {p.number}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button size="sm" onClick={openCreate} className="bg-orange-600 hover:bg-orange-700">
              <Plus size={14} className="mr-1" /> Novo bico
            </Button>
          </div>
        }
      />

      <div className="flex-1 overflow-auto p-6">
        {pumpsLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : filteredNozzles.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum bico cadastrado neste posto.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Bico</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Bomba</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Combustível</th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Status</th>
                  <th className="px-4 py-2" />
                </tr>
              </thead>
              <tbody>
                {filteredNozzles.map((nozzle) => (
                  <tr key={nozzle.id} className="border-b last:border-0">
                    <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">Bico {nozzle.number}</td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">
                      Bomba {pumpById[nozzle.pumpId]?.number ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">
                      {fuelById[nozzle.fuelId]?.name ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <StatusToggle
                        id={nozzle.id}
                        active={nozzle.active}
                        queryKeys={queryKeysForNozzle(nozzle.pumpId)}
                        onToggle={(id, active) => patchNozzleStatus(id, active)}
                      />
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(nozzle)}
                        className="text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300"
                        title="Editar"
                      >
                        <Pencil size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <NozzleSheet open={sheetOpen} onOpenChange={setSheetOpen} nozzle={editNozzle} />
    </div>
  )
}
