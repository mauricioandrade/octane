import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Download, X } from 'lucide-react'
import { toast } from 'sonner'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Separator } from '@/components/ui/separator'
import { getShiftSummary, cancelFueling } from '@/api/fuelings'
import { getReconciliation } from '@/api/shifts'
import { formatBRL, formatLiters } from '@/lib/utils'
import { exportReconciliationCSV } from '@/lib/export'
import { PAYMENT_METHOD_LABELS, type PaymentMethod } from '@/types'
import type { Shift, ReconciliationLine } from '@/types'

type Props = {
  shift: Shift
  open: boolean
  onOpenChange: (open: boolean) => void
}

function divergenceBadge(line: ReconciliationLine) {
  const div = Math.abs(line.divergenceLiters)
  if (div === 0)
    return <Badge className="bg-green-100 text-green-700 hover:bg-green-100">0 L</Badge>
  const pct = line.measuredLiters > 0 ? (div / line.measuredLiters) * 100 : 0
  if (pct <= 0.6)
    return (
      <Badge className="bg-yellow-100 text-yellow-700 hover:bg-yellow-100">
        {formatLiters(div)} ({pct.toFixed(2)}%)
      </Badge>
    )
  return (
    <Badge className="bg-red-100 text-red-700 hover:bg-red-100">
      {formatLiters(div)} ({pct.toFixed(2)}%)
    </Badge>
  )
}

export function ShiftDetailModal({ shift, open, onOpenChange }: Props) {
  const qc = useQueryClient()

  const { data: summary, isLoading: summaryLoading } = useQuery({
    queryKey: ['shift-summary', shift.id],
    queryFn: () => getShiftSummary(shift.id),
    enabled: open,
  })

  const cancelMutation = useMutation({
    mutationFn: (fuelingId: string) => cancelFueling(shift.id, fuelingId),
    onSuccess: () => {
      toast.success('Abastecimento cancelado')
      qc.invalidateQueries({ queryKey: ['shift-summary', shift.id] })
      qc.invalidateQueries({ queryKey: ['shift', 'open', shift.stationId] })
    },
    onError: () => {
      toast.error('Erro ao cancelar abastecimento')
    },
  })

  const { data: reconciliation } = useQuery({
    queryKey: ['reconciliation', shift.id],
    queryFn: () => getReconciliation(shift.id),
    enabled: open && shift.status === 'CLOSED',
  })

  const duration =
    shift.closedAt
      ? Math.round((new Date(shift.closedAt).getTime() - new Date(shift.openedAt).getTime()) / 60_000)
      : null

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-2xl max-h-[80vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            Turno — {shift.employeeName}
            <Badge
              className={
                shift.status === 'OPEN'
                  ? 'bg-green-100 text-green-700 hover:bg-green-100'
                  : 'bg-slate-100 text-slate-500 hover:bg-slate-100'
              }
            >
              {shift.status === 'OPEN' ? 'Aberto' : 'Fechado'}
            </Badge>
          </DialogTitle>
        </DialogHeader>

        {/* Resumo */}
        <div className="grid grid-cols-2 gap-2 text-sm">
          <div>
            <span className="text-slate-400 dark:text-slate-500">Abertura:</span>{' '}
            <span className="font-medium">
              {new Date(shift.openedAt).toLocaleString('pt-BR')}
            </span>
          </div>
          {shift.closedAt && (
            <div>
              <span className="text-slate-400 dark:text-slate-500">Fechamento:</span>{' '}
              <span className="font-medium">
                {new Date(shift.closedAt).toLocaleString('pt-BR')}
              </span>
            </div>
          )}
          {duration !== null && (
            <div>
              <span className="text-slate-400 dark:text-slate-500">Duração:</span>{' '}
              <span className="font-medium">
                {duration >= 60
                  ? `${Math.floor(duration / 60)}h ${duration % 60}min`
                  : `${duration}min`}
              </span>
            </div>
          )}
        </div>

        {summaryLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : summary && (
          <>
            {/* Totais */}
            <div className="flex gap-4 rounded-lg border bg-slate-50 dark:bg-slate-800 p-3 text-sm">
              <div>
                <p className="text-[10px] uppercase text-slate-400 dark:text-slate-500">Volume</p>
                <p className="font-bold">{formatLiters(summary.totalLiters)}</p>
              </div>
              <div>
                <p className="text-[10px] uppercase text-slate-400 dark:text-slate-500">Receita</p>
                <p className="font-bold text-orange-600">{formatBRL(summary.totalAmount)}</p>
              </div>
              <div>
                <p className="text-[10px] uppercase text-slate-400 dark:text-slate-500">Abastecimentos</p>
                <p className="font-bold">{summary.fuelings.length}</p>
              </div>
            </div>

            <Separator />

            {/* Abastecimentos */}
            <div>
              <p className="mb-2 text-sm font-semibold text-slate-700 dark:text-slate-300">Abastecimentos</p>
              {summary.fuelings.length === 0 ? (
                <p className="text-xs text-slate-400 dark:text-slate-500">Nenhum abastecimento neste turno.</p>
              ) : (
                <div className="overflow-hidden rounded-lg border">
                  <table className="w-full text-xs">
                    <thead className="bg-slate-50 dark:bg-slate-800">
                      <tr>
                        <th className="px-3 py-2 text-left font-semibold text-slate-400 dark:text-slate-500">Horário</th>
                        <th className="px-3 py-2 text-left font-semibold text-slate-400 dark:text-slate-500">Bico</th>
                        <th className="px-3 py-2 text-left font-semibold text-slate-400 dark:text-slate-500">Combustível</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-400 dark:text-slate-500">Litros</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-400 dark:text-slate-500">Total</th>
                        <th className="px-3 py-2 text-left font-semibold text-slate-400 dark:text-slate-500">Pgto</th>
                        {shift.status === 'OPEN' && <th className="px-3 py-2" />}
                      </tr>
                    </thead>
                    <tbody>
                      {summary.fuelings.map((f) => (
                        <tr key={f.id} className="border-t">
                          <td className="px-3 py-1.5 text-slate-400 dark:text-slate-500">
                            {new Date(f.fueledAt).toLocaleTimeString('pt-BR', {
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                          </td>
                          <td className="px-3 py-1.5 font-medium">B{f.nozzleNumber}</td>
                          <td className="px-3 py-1.5 text-slate-600 dark:text-slate-400">{f.fuelName}</td>
                          <td className="px-3 py-1.5 text-right tabular-nums">
                            {formatLiters(f.liters)}
                          </td>
                          <td className="px-3 py-1.5 text-right font-semibold tabular-nums text-orange-600">
                            {formatBRL(f.totalAmount)}
                          </td>
                          <td className="px-3 py-1.5 text-slate-400 dark:text-slate-500">
                            {PAYMENT_METHOD_LABELS[f.paymentMethod as PaymentMethod] ?? f.paymentMethod}
                          </td>
                          {shift.status === 'OPEN' && (
                            <td className="px-3 py-1.5">
                              <Button
                                variant="ghost"
                                size="icon"
                                className="h-6 w-6 text-slate-400 hover:text-red-500"
                                aria-label="Cancelar abastecimento"
                                disabled={cancelMutation.isPending}
                                onClick={() => {
                                  if (window.confirm('Cancelar este abastecimento?')) {
                                    cancelMutation.mutate(f.id)
                                  }
                                }}
                              >
                                <X size={14} />
                              </Button>
                            </td>
                          )}
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            {/* Reconciliação (só turno fechado) */}
            {reconciliation && (
              <>
                <Separator />
                <div>
                  <p className="mb-2 text-sm font-semibold text-slate-700 dark:text-slate-300">Reconciliação</p>
                  <div className="overflow-hidden rounded-lg border">
                    <table className="w-full text-xs">
                      <thead className="bg-slate-50 dark:bg-slate-800">
                        <tr>
                          <th className="px-3 py-2 text-left font-semibold text-slate-400 dark:text-slate-500">Bico</th>
                          <th className="px-3 py-2 text-right font-semibold text-slate-400 dark:text-slate-500">Medido</th>
                          <th className="px-3 py-2 text-right font-semibold text-slate-400 dark:text-slate-500">Lançado</th>
                          <th className="px-3 py-2 text-right font-semibold text-slate-400 dark:text-slate-500">Divergência</th>
                        </tr>
                      </thead>
                      <tbody>
                        {reconciliation.lines.map((line) => (
                          <tr key={line.nozzleId} className="border-t">
                            <td className="px-3 py-1.5 font-medium">
                              B{line.nozzleNumber} · {line.fuelName}
                            </td>
                            <td className="px-3 py-1.5 text-right tabular-nums">
                              {formatLiters(line.measuredLiters)}
                            </td>
                            <td className="px-3 py-1.5 text-right tabular-nums">
                              {formatLiters(line.fueledLiters)}
                            </td>
                            <td className="px-3 py-1.5 text-right">
                              {divergenceBadge(line)}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                      <tfoot className="border-t bg-slate-50 dark:bg-slate-800">
                        <tr>
                          <td className="px-3 py-2 font-semibold" colSpan={2}>
                            Total medido
                          </td>
                          <td className="px-3 py-2 text-right font-semibold tabular-nums" colSpan={2}>
                            {formatLiters(reconciliation.totalMeasuredLiters)}
                          </td>
                        </tr>
                      </tfoot>
                    </table>
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => exportReconciliationCSV(reconciliation, `reconciliacao-${shift.id.slice(0, 8)}.csv`)}
                  >
                    <Download size={14} className="mr-1" />
                    Exportar CSV
                  </Button>
                </div>
              </>
            )}
          </>
        )}
      </DialogContent>
    </Dialog>
  )
}
