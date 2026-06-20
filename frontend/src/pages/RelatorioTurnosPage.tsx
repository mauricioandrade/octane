import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Download } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { TopBar } from '@/components/layout/TopBar'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getShiftReport } from '@/api/reports'
import { formatBRL, formatLiters } from '@/lib/utils'

function today() {
  return new Date().toISOString().slice(0, 10)
}
function weekAgo() {
  const d = new Date()
  d.setDate(d.getDate() - 7)
  return d.toISOString().slice(0, 10)
}

function formatDuration(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = minutes % 60
  return h > 0 ? `${h}h${m > 0 ? ` ${m}min` : ''}` : `${m}min`
}

export function RelatorioTurnosPage() {
  const { station } = useActiveStation()
  const [from, setFrom] = useState(weekAgo)
  const [to, setTo] = useState(today)

  const { data, isLoading } = useQuery({
    queryKey: ['report-shifts', station?.id, from, to],
    queryFn: () => getShiftReport(station!.id, from, to),
    enabled: !!station && !!from && !!to,
  })

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center text-sm text-slate-400">
        Selecione um posto.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Relatório de Turnos" />
      <div className="flex flex-1 flex-col gap-6 overflow-auto p-6">
        <div className="flex items-end gap-3">
          <div>
            <label className="text-[10px] uppercase text-slate-400">De</label>
            <Input type="date" value={from} onChange={e => setFrom(e.target.value)} className="w-40" />
          </div>
          <div>
            <label className="text-[10px] uppercase text-slate-400">Até</label>
            <Input type="date" value={to} onChange={e => setTo(e.target.value)} className="w-40" />
          </div>
          {data && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => window.open(`/api/reports/shifts/csv?stationId=${station.id}&from=${from}&to=${to}`, '_blank')}
            >
              <Download size={14} className="mr-1" /> Exportar CSV
            </Button>
          )}
        </div>

        {isLoading ? (
          <Skeleton className="h-64 w-full" />
        ) : data ? (
          <div className="rounded-lg border bg-white dark:bg-slate-900 overflow-hidden">
            <table className="w-full text-xs">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-slate-400">Frentista</th>
                  <th className="px-4 py-2 text-left text-slate-400">Abertura</th>
                  <th className="px-4 py-2 text-left text-slate-400">Fechamento</th>
                  <th className="px-4 py-2 text-right text-slate-400">Duração</th>
                  <th className="px-4 py-2 text-right text-slate-400">Receita</th>
                  <th className="px-4 py-2 text-right text-slate-400">Litros</th>
                  <th className="px-4 py-2 text-right text-slate-400">Abast.</th>
                </tr>
              </thead>
              <tbody>
                {data.shifts.map((s, i) => (
                  <tr key={i} className="border-b last:border-0">
                    <td className="px-4 py-2 font-medium text-slate-700 dark:text-slate-300">{s.employeeName}</td>
                    <td className="px-4 py-2 text-slate-500">{new Date(s.openedAt).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}</td>
                    <td className="px-4 py-2 text-slate-500">{new Date(s.closedAt).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}</td>
                    <td className="px-4 py-2 text-right text-slate-500">{formatDuration(s.durationMinutes)}</td>
                    <td className="px-4 py-2 text-right font-medium text-slate-700 dark:text-slate-300">{formatBRL(s.revenue)}</td>
                    <td className="px-4 py-2 text-right text-slate-500">{formatLiters(s.liters)}</td>
                    <td className="px-4 py-2 text-right text-slate-500">{s.fuelingCount}</td>
                  </tr>
                ))}
              </tbody>
              {data.shifts.length > 0 && (
                <tfoot>
                  <tr className="border-t-2 bg-slate-50 dark:bg-slate-800 font-semibold">
                    <td className="px-4 py-2 text-slate-700 dark:text-slate-300" colSpan={4}>Total</td>
                    <td className="px-4 py-2 text-right text-slate-700 dark:text-slate-300">{formatBRL(data.totalRevenue)}</td>
                    <td className="px-4 py-2 text-right text-slate-500">{formatLiters(data.totalLiters)}</td>
                    <td className="px-4 py-2 text-right text-slate-500">{data.totalFuelings}</td>
                  </tr>
                </tfoot>
              )}
            </table>
            {data.shifts.length === 0 && (
              <p className="p-4 text-sm text-slate-400">Nenhum turno fechado no período.</p>
            )}
          </div>
        ) : null}
      </div>
    </div>
  )
}
