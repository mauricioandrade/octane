import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Pencil, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { StationSheet } from '@/components/cadastros/StationSheet'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { getStations, patchStationStatus } from '@/api/stations'
import type { Station } from '@/types'

export function PostosPage() {
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editStation, setEditStation] = useState<Station | undefined>()

  const { data: stations = [], isLoading } = useQuery({
    queryKey: ['stations'],
    queryFn: () => getStations(),
  })

  function openCreate() {
    setEditStation(undefined)
    setSheetOpen(true)
  }

  function openEdit(station: Station) {
    setEditStation(station)
    setSheetOpen(true)
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title="Postos"
        actions={
          <Button size="sm" onClick={openCreate} className="bg-orange-600 hover:bg-orange-700">
            <Plus size={14} className="mr-1" /> Novo posto
          </Button>
        }
      />

      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : stations.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum posto cadastrado.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Nome</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">CNPJ</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Cidade</th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Status</th>
                  <th className="px-4 py-2" />
                </tr>
              </thead>
              <tbody>
                {stations.map((s) => (
                  <tr key={s.id} className="border-b last:border-0">
                    <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">{s.name}</td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">{s.cnpj}</td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">{s.city} — {s.state}</td>
                    <td className="px-4 py-3 text-center">
                      <StatusToggle
                        id={s.id}
                        active={s.active}
                        queryKeys={[['stations']]}
                        onToggle={(id, active) => patchStationStatus(id, active)}
                      />
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(s)}
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

      <StationSheet
        open={sheetOpen}
        onOpenChange={setSheetOpen}
        station={editStation}
      />
    </div>
  )
}
