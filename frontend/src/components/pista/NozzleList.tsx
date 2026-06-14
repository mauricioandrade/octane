import { useState } from 'react'
import { useQuery, useQueries } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { FuelingForm } from './FuelingForm'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getPumps } from '@/api/pumps'
import { getNozzles } from '@/api/nozzles'
import { getFuels } from '@/api/fuels'
import { formatLiters } from '@/lib/utils'
import type { ShiftSummary } from '@/types'

type Props = {
  shiftId: string
  summary: ShiftSummary | undefined
}

export function NozzleList({ shiftId, summary }: Props) {
  const { station } = useActiveStation()
  const [expandedNozzleId, setExpandedNozzleId] = useState<string | null>(null)

  const { data: pumps = [], isLoading: pumpsLoading } = useQuery({
    queryKey: ['pumps', station?.id, 'ACTIVE'],
    queryFn: () => getPumps(station!.id, 'ACTIVE'),
    enabled: !!station,
  })

  const nozzleResults = useQueries({
    queries: pumps.map((pump) => ({
      queryKey: ['nozzles', pump.id, 'active'],
      queryFn: () => getNozzles(pump.id, true),
      enabled: pumps.length > 0,
    })),
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
    enabled: !!station,
  })

  const fuelById = Object.fromEntries(fuels.map((f) => [f.id, f]))

  // Subtotais por bico do turno atual
  const nozzleTotals = Object.fromEntries(
    (summary?.fuelings ?? []).reduce((map, f) => {
      const prev = map.get(f.nozzleId) ?? { count: 0, liters: 0 }
      map.set(f.nozzleId, { count: prev.count + 1, liters: prev.liters + f.liters })
      return map
    }, new Map<string, { count: number; liters: number }>()),
  )

  if (pumpsLoading) {
    return (
      <div className="flex flex-col gap-2">
        <Skeleton className="h-12 w-full" />
        <Skeleton className="h-12 w-full" />
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-3">
      {pumps.map((pump, idx) => {
        const nozzles = nozzleResults[idx]?.data ?? []
        return (
          <div key={pump.id} className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <div className="border-b bg-slate-50 dark:bg-slate-800 px-4 py-2 text-xs font-semibold text-slate-500 dark:text-slate-400">
              Bomba {pump.number}
            </div>
            {nozzles.map((nozzle) => {
              const fuel = fuelById[nozzle.fuelId]
              const totals = nozzleTotals[nozzle.id]
              const isExpanded = expandedNozzleId === nozzle.id

              return (
                <div key={nozzle.id} className="border-b last:border-0">
                  <div className="flex items-center justify-between px-4 py-2.5">
                    <div className="flex items-center gap-3">
                      <span className="rounded bg-slate-100 dark:bg-slate-800 px-2 py-0.5 text-xs font-bold text-slate-500 dark:text-slate-400">
                        B{nozzle.number}
                      </span>
                      <div>
                        <p className="text-sm font-semibold text-slate-800 dark:text-slate-200">
                          {fuel?.name ?? '—'}
                        </p>
                        <p className="text-xs text-slate-400 dark:text-slate-500">
                          {totals
                            ? `${totals.count} abast. · ${formatLiters(totals.liters)}`
                            : '0 abast. · 0,000 L'}
                        </p>
                      </div>
                    </div>
                    <Button
                      size="sm"
                      onClick={() =>
                        setExpandedNozzleId(isExpanded ? null : nozzle.id)
                      }
                      className="bg-orange-600 hover:bg-orange-700 text-xs"
                    >
                      {isExpanded ? '✕' : <><Plus size={12} className="mr-1" />Registrar</>}
                    </Button>
                  </div>

                  {isExpanded && (
                    <FuelingForm
                      shiftId={shiftId}
                      nozzleId={nozzle.id}
                      nozzleNumber={nozzle.number}
                      fuelName={fuel?.name ?? ''}
                      onClose={() => setExpandedNozzleId(null)}
                    />
                  )}
                </div>
              )
            })}
          </div>
        )
      })}
    </div>
  )
}
