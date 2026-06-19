import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { RefreshCw } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { TopBar } from '@/components/layout/TopBar'
import { CommissionEntryCard } from '@/components/commission/CommissionEntryCard'
import { useActiveStation } from '@/hooks/useActiveStation'
import { listCommissionEntries } from '@/api/commissions'
import { formatBRL } from '@/lib/utils'

type PaidFilter = 'all' | 'pending' | 'paid'

export function ComissaoEntradasPage() {
  const { station } = useActiveStation()
  const [page, setPage] = useState(0)
  const [paidFilter, setPaidFilter] = useState<PaidFilter>('all')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')

  const paidParam =
    paidFilter === 'pending' ? false : paidFilter === 'paid' ? true : undefined

  const { data, isLoading, refetch, isFetching } = useQuery({
    queryKey: ['commission-entries', station?.id, paidFilter, fromDate, toDate, page],
    queryFn: () =>
      listCommissionEntries(station!.id, {
        paid: paidParam,
        from: fromDate || undefined,
        to: toDate || undefined,
        page,
      }),
    enabled: !!station,
  })

  const entries = data?.content ?? []

  const totalPending = entries
    .filter((e) => !e.paid)
    .reduce((sum, e) => sum + e.commission, 0)

  const totalPaid = entries
    .filter((e) => e.paid)
    .reduce((sum, e) => sum + e.commission, 0)

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center p-6 text-sm text-slate-400">
        Selecione um posto para ver as comissões.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Entradas de Comissão" />

      <div className="flex flex-1 flex-col gap-4 overflow-auto p-6">
        {/* Summary */}
        <div className="grid grid-cols-2 gap-3">
          <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
            <p className="text-xs text-slate-400 dark:text-slate-500">A pagar (pendentes)</p>
            <p className="mt-1 text-lg font-bold text-yellow-600 dark:text-yellow-400">
              {formatBRL(totalPending)}
            </p>
          </div>
          <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
            <p className="text-xs text-slate-400 dark:text-slate-500">Pago no período</p>
            <p className="mt-1 text-lg font-bold text-green-600 dark:text-green-400">
              {formatBRL(totalPaid)}
            </p>
          </div>
        </div>

        {/* Filtros */}
        <div className="flex flex-wrap items-center gap-3">
          <Select
            value={paidFilter}
            onValueChange={(v) => { setPaidFilter(v as PaidFilter); setPage(0) }}
          >
            <SelectTrigger className="h-8 w-36 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Todas</SelectItem>
              <SelectItem value="pending">Pendentes</SelectItem>
              <SelectItem value="paid">Pagas</SelectItem>
            </SelectContent>
          </Select>

          <div className="flex items-center gap-1">
            <Input
              type="date"
              value={fromDate}
              onChange={(e) => { setFromDate(e.target.value); setPage(0) }}
              className="h-8 w-36 text-xs"
            />
            <span className="text-slate-400 text-xs">até</span>
            <Input
              type="date"
              value={toDate}
              onChange={(e) => { setToDate(e.target.value); setPage(0) }}
              className="h-8 w-36 text-xs"
            />
          </div>

          {(paidFilter !== 'all' || fromDate || toDate) && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => { setPaidFilter('all'); setFromDate(''); setToDate(''); setPage(0) }}
              className="text-xs text-slate-400"
            >
              Limpar filtros
            </Button>
          )}

          <Button
            variant="outline"
            size="sm"
            onClick={() => refetch()}
            disabled={isFetching}
            className="ml-auto h-8 text-xs"
          >
            <RefreshCw size={13} className={isFetching ? 'animate-spin mr-1' : 'mr-1'} />
            Atualizar
          </Button>
        </div>

        {/* List */}
        {isLoading ? (
          <div className="flex flex-col gap-3">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-28 w-full rounded-lg" />
            ))}
          </div>
        ) : entries.length === 0 ? (
          <div className="flex flex-1 items-center justify-center py-16 text-sm text-slate-400">
            Nenhuma comissão encontrada.
          </div>
        ) : (
          <div className="flex flex-col gap-3">
            {entries.map((entry) => (
              <CommissionEntryCard key={entry.id} entry={entry} stationId={station.id} />
            ))}
          </div>
        )}

        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between pt-2">
            <span className="text-xs text-slate-400">
              {data.totalElements} entradas · página {data.page + 1} de {data.totalPages}
            </span>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                disabled={data.page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                ← Anterior
              </Button>
              <Button
                variant="outline"
                size="sm"
                disabled={data.page >= data.totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Próxima →
              </Button>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
