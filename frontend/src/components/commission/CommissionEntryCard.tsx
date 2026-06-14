import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { formatBRL } from '@/lib/utils'
import { markCommissionPaid } from '@/api/commissions'
import type { CommissionEntry } from '@/types'

type Props = {
  entry: CommissionEntry
  stationId: string
}

function formatDateTime(iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  }).format(new Date(iso))
}

export function CommissionEntryCard({ entry, stationId }: Props) {
  const qc = useQueryClient()

  const payMutation = useMutation({
    mutationFn: () => markCommissionPaid(entry.id),
    onSuccess: () => {
      toast.success('Comissão marcada como paga!')
      qc.invalidateQueries({ queryKey: ['commission-entries', stationId] })
    },
    onError: () => {
      toast.error('Erro ao marcar comissão como paga')
    },
  })

  return (
    <div className="flex flex-col gap-2 rounded-lg border bg-white dark:bg-slate-900 p-4 shadow-sm">
      <div className="flex items-start justify-between gap-2">
        <div>
          <p className="text-sm font-semibold text-slate-800 dark:text-slate-100">
            {entry.employeeName}
          </p>
          <p className="text-xs text-slate-400 dark:text-slate-500 mt-0.5">
            Turno: {formatDateTime(entry.createdAt)}
          </p>
        </div>
        <Badge
          variant={entry.paid ? 'default' : 'secondary'}
          className={
            entry.paid
              ? 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300'
              : 'bg-yellow-100 text-yellow-700 dark:bg-yellow-900 dark:text-yellow-300'
          }
        >
          {entry.paid ? 'Pago' : 'Pendente'}
        </Badge>
      </div>

      <div className="grid grid-cols-3 gap-2 text-xs">
        <div>
          <p className="text-slate-400 dark:text-slate-500">Base</p>
          <p className="font-medium text-slate-700 dark:text-slate-200">
            {formatBRL(entry.baseAmount)}
          </p>
        </div>
        <div>
          <p className="text-slate-400 dark:text-slate-500">Taxa</p>
          <p className="font-medium text-slate-700 dark:text-slate-200">
            {(entry.rate * 100).toFixed(2)}%
          </p>
        </div>
        <div>
          <p className="text-slate-400 dark:text-slate-500">Comissão</p>
          <p className="font-semibold text-orange-600 dark:text-orange-400">
            {formatBRL(entry.commission)}
          </p>
        </div>
      </div>

      {entry.paid && entry.paidAt && (
        <p className="text-xs text-slate-400 dark:text-slate-500">
          Pago em: {formatDateTime(entry.paidAt)}
        </p>
      )}

      {!entry.paid && (
        <Button
          size="sm"
          variant="outline"
          disabled={payMutation.isPending}
          onClick={() => payMutation.mutate()}
          className="self-start text-xs"
        >
          {payMutation.isPending ? 'Registrando…' : 'Marcar como pago'}
        </Button>
      )}
    </div>
  )
}
