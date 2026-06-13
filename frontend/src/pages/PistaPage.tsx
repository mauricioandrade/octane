import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Fuel } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { OpenShiftSheet } from '@/components/pista/OpenShiftSheet'
import { NozzleList } from '@/components/pista/NozzleList'
import { useActiveStation } from '@/hooks/useActiveStation'
import { useShift } from '@/hooks/useShift'
import { getShiftSummary } from '@/api/fuelings'
import { formatBRL, formatLiters } from '@/lib/utils'

function MetricCard({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-lg border bg-white p-4">
      <p className="text-[10px] uppercase tracking-wider text-slate-400">{label}</p>
      <p className="mt-1 text-xl font-bold text-slate-900">{value}</p>
    </div>
  )
}

export function PistaPage() {
  const { station } = useActiveStation()
  const { data: shift, isLoading } = useShift()
  const [openSheetOpen, setOpenSheetOpen] = useState(false)

  const { data: summary } = useQuery({
    queryKey: ['shift-summary', shift?.id],
    queryFn: () => getShiftSummary(shift!.id),
    enabled: !!shift,
    refetchInterval: 30_000,
  })

  const lastFueledAt =
    summary?.fuelings.length
      ? new Date(
          Math.max(...summary.fuelings.map((f) => new Date(f.fueledAt).getTime())),
        )
      : null

  function relativeTime(date: Date): string {
    const diffMs = Date.now() - date.getTime()
    const diffMin = Math.floor(diffMs / 60_000)
    if (diffMin < 1) return 'agora'
    if (diffMin < 60) return `há ${diffMin} min`
    return `há ${Math.floor(diffMin / 60)} h`
  }

  if (!station) {
    return (
      <div className="flex flex-1 flex-col items-center justify-center gap-4 text-slate-400">
        <Fuel size={40} />
        <p className="text-sm">Selecione um posto na barra lateral para continuar.</p>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title="Pista"
        actions={
          shift ? (
            <div className="flex items-center gap-2">
              <span className="rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700">
                ● Turno aberto · {shift.employeeName}
              </span>
              {/* Fechar turno: Task 08 */}
            </div>
          ) : undefined
        }
      />

      <div className="flex flex-1 flex-col gap-4 overflow-auto p-6">
        {isLoading ? (
          <div className="flex flex-col gap-3">
            <Skeleton className="h-10 w-64" />
            <Skeleton className="h-32 w-full" />
          </div>
        ) : !shift ? (
          <div className="flex flex-1 flex-col items-center justify-center gap-4">
            <p className="text-slate-500">Nenhum turno aberto.</p>
            <Button
              onClick={() => setOpenSheetOpen(true)}
              className="bg-orange-600 hover:bg-orange-700"
            >
              Abrir turno
            </Button>
          </div>
        ) : (
          <>
            {/* Métricas */}
            <div className="grid grid-cols-4 gap-3">
              <MetricCard
                label="Volume total"
                value={formatLiters(summary?.totalLiters ?? 0)}
              />
              <MetricCard
                label="Receita"
                value={formatBRL(summary?.totalAmount ?? 0)}
              />
              <MetricCard
                label="Abastecimentos"
                value={String(summary?.fuelings.length ?? 0)}
              />
              <MetricCard
                label="Últ. abastecimento"
                value={lastFueledAt ? relativeTime(lastFueledAt) : '—'}
              />
            </div>

            {/* Lista de bicos */}
            <NozzleList shiftId={shift.id} summary={summary} />
          </>
        )}
      </div>

      <OpenShiftSheet open={openSheetOpen} onOpenChange={setOpenSheetOpen} />
    </div>
  )
}
