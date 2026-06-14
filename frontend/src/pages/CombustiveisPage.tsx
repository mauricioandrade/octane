import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, Pencil } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { TopBar } from '@/components/layout/TopBar'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { FuelSheet } from '@/components/cadastros/FuelSheet'
import { getFuels, patchFuelStatus } from '@/api/fuels'
import type { Fuel } from '@/types'

export function CombustiveisPage() {
  const { data: fuels = [], isLoading } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
  })

  const [sheetOpen, setSheetOpen] = useState(false)
  const [editFuel, setEditFuel] = useState<Fuel | undefined>()

  function openCreate() {
    setEditFuel(undefined)
    setSheetOpen(true)
  }

  function openEdit(fuel: Fuel) {
    setEditFuel(fuel)
    setSheetOpen(true)
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Combustíveis" subtitle="Lista global — ative ou inative conforme disponibilidade" />

      <div className="flex-1 overflow-auto p-6">
        <div className="mb-4 flex justify-end">
          <Button
            size="sm"
            className="bg-orange-600 hover:bg-orange-700"
            onClick={openCreate}
          >
            <Plus size={14} className="mr-1" />
            Novo combustível
          </Button>
        </div>

        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : fuels.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum combustível cadastrado no sistema.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Nome
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Unidade
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
                {fuels.map((fuel) => (
                  <tr key={fuel.id} className="border-b last:border-0">
                    <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">{fuel.name}</td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">{fuel.unit}</td>
                    <td className="px-4 py-3 text-center">
                      <StatusToggle
                        id={fuel.id}
                        active={fuel.active}
                        queryKeys={[['fuels']]}
                        onToggle={(id, active) => patchFuelStatus(id, active)}
                      />
                    </td>
                    <td className="px-4 py-3 text-center">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-7 w-7"
                        onClick={() => openEdit(fuel)}
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

      <FuelSheet
        open={sheetOpen}
        onOpenChange={setSheetOpen}
        fuel={editFuel}
      />
    </div>
  )
}
