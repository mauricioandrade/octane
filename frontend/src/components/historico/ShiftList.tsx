import { useState } from 'react'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { ShiftDetailModal } from './ShiftDetailModal'
import type { Shift, Page } from '@/types'

type Props = {
  data: Page<Shift> | undefined
  isLoading: boolean
}

export function ShiftList({ data, isLoading }: Props) {
  const [selectedShift, setSelectedShift] = useState<Shift | null>(null)

  if (isLoading) {
    return (
      <div className="flex flex-col gap-2">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-10 w-full" />
      </div>
    )
  }

  if (!data || data.content.length === 0) {
    return <p className="text-sm text-slate-400">Nenhum turno encontrado.</p>
  }

  function formatDuration(shift: Shift): string {
    if (!shift.closedAt) return '—'
    const diffMin = Math.round(
      (new Date(shift.closedAt).getTime() - new Date(shift.openedAt).getTime()) / 60_000,
    )
    if (diffMin >= 60) return `${Math.floor(diffMin / 60)}h ${diffMin % 60}min`
    return `${diffMin}min`
  }

  return (
    <>
      <div className="overflow-hidden rounded-lg border bg-white">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-slate-50">
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Frentista</th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Abertura</th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Fechamento</th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Duração</th>
              <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400">Status</th>
            </tr>
          </thead>
          <tbody>
            {data.content.map((shift) => (
              <tr
                key={shift.id}
                className="cursor-pointer border-b last:border-0 hover:bg-slate-50"
                onClick={() => setSelectedShift(shift)}
              >
                <td className="px-4 py-3 font-medium text-slate-800">{shift.employeeName}</td>
                <td className="px-4 py-3 text-slate-500">
                  {new Date(shift.openedAt).toLocaleString('pt-BR', {
                    day: '2-digit',
                    month: '2-digit',
                    hour: '2-digit',
                    minute: '2-digit',
                  })}
                </td>
                <td className="px-4 py-3 text-slate-500">
                  {shift.closedAt
                    ? new Date(shift.closedAt).toLocaleString('pt-BR', {
                        day: '2-digit',
                        month: '2-digit',
                        hour: '2-digit',
                        minute: '2-digit',
                      })
                    : '—'}
                </td>
                <td className="px-4 py-3 text-slate-500">{formatDuration(shift)}</td>
                <td className="px-4 py-3 text-center">
                  <Badge
                    className={
                      shift.status === 'OPEN'
                        ? 'bg-green-100 text-green-700 hover:bg-green-100'
                        : 'bg-slate-100 text-slate-500 hover:bg-slate-100'
                    }
                  >
                    {shift.status === 'OPEN' ? 'Aberto' : 'Fechado'}
                  </Badge>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {selectedShift && (
        <ShiftDetailModal
          shift={selectedShift}
          open={!!selectedShift}
          onOpenChange={(open) => !open && setSelectedShift(null)}
        />
      )}
    </>
  )
}
