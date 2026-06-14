import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Pencil, Plus } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Badge } from '@/components/ui/badge'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { TopBar } from '@/components/layout/TopBar'
import { PumpSheet } from '@/components/cadastros/PumpSheet'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getPumps, patchPumpStatus } from '@/api/pumps'
import type { Pump, PumpStatus } from '@/types'

const STATUS_LABELS: Record<PumpStatus, string> = {
  ACTIVE: 'Ativa',
  INACTIVE: 'Inativa',
  MAINTENANCE: 'Manutenção',
}

const STATUS_COLORS: Record<PumpStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-700',
  INACTIVE: 'bg-slate-100 text-slate-400',
  MAINTENANCE: 'bg-yellow-100 text-yellow-700',
}

export function BombasPage() {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editPump, setEditPump] = useState<Pump | undefined>()

  const { data: pumps = [], isLoading } = useQuery({
    queryKey: ['pumps', station?.id],
    queryFn: () => getPumps(station!.id),
    enabled: !!station,
  })

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: PumpStatus }) =>
      patchPumpStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['pumps', station?.id] })
    },
    onError: () => toast.error('Erro ao alterar status'),
  })

  function openCreate() {
    setEditPump(undefined)
    setSheetOpen(true)
  }

  function openEdit(pump: Pump) {
    setEditPump(pump)
    setSheetOpen(true)
  }

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center p-6 text-sm text-slate-400">
        Selecione um posto para gerenciar bombas.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title="Bombas"
        actions={
          <Button size="sm" onClick={openCreate} className="bg-orange-600 hover:bg-orange-700">
            <Plus size={14} className="mr-1" /> Nova bomba
          </Button>
        }
      />

      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : pumps.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhuma bomba cadastrada neste posto.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Número</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Status</th>
                  <th className="px-4 py-2" />
                </tr>
              </thead>
              <tbody>
                {pumps.map((pump) => (
                  <tr key={pump.id} className="border-b last:border-0">
                    <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">Bomba {pump.number}</td>
                    <td className="px-4 py-3">
                      <Select
                        value={pump.status}
                        onValueChange={(value) =>
                          statusMutation.mutate({ id: pump.id, status: value as PumpStatus })
                        }
                      >
                        <SelectTrigger className="w-36 h-7 text-xs">
                          <SelectValue>
                            <Badge className={STATUS_COLORS[pump.status]}>
                              {STATUS_LABELS[pump.status]}
                            </Badge>
                          </SelectValue>
                        </SelectTrigger>
                        <SelectContent>
                          {(Object.keys(STATUS_LABELS) as PumpStatus[]).map((s) => (
                            <SelectItem key={s} value={s} className="text-xs">
                              {STATUS_LABELS[s]}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(pump)}
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

      <PumpSheet open={sheetOpen} onOpenChange={setSheetOpen} pump={editPump} />
    </div>
  )
}
