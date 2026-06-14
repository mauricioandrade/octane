import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { TopBar } from '@/components/layout/TopBar'
import { ShiftList } from '@/components/historico/ShiftList'
import { useActiveStation } from '@/hooks/useActiveStation'
import { listShifts } from '@/api/shifts'

export function HistoricoPage() {
  const { station } = useActiveStation()
  const [page, setPage] = useState(0)
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [fromDate, setFromDate] = useState('')
  const [toDate, setToDate] = useState('')

  const dateRangeValid = !fromDate || !toDate || fromDate <= toDate

  const { data, isLoading } = useQuery({
    queryKey: ['shifts', station?.id, page, statusFilter, fromDate, toDate],
    queryFn: () =>
      listShifts(station!.id, {
        page,
        size: 10,
        status: statusFilter === 'all' ? undefined : statusFilter,
        from: fromDate ? `${fromDate}T00:00:00` : undefined,
        to: toDate ? `${toDate}T23:59:59` : undefined,
      }),
    enabled: !!station && dateRangeValid,
  })

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center p-6 text-sm text-slate-400">
        Selecione um posto para ver o histórico.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Histórico de turnos" />

      <div className="flex flex-1 flex-col gap-4 overflow-auto p-6">
        {/* Filtros */}
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex items-center gap-1">
            <Input
              type="date"
              value={fromDate}
              onChange={(e) => {
                const next = e.target.value
                setFromDate(next)
                setPage(0)
                if (next && toDate && next > toDate) {
                  toast.error('Data inicial deve ser anterior à data final')
                }
              }}
              className="h-8 w-36 text-xs"
            />
            <span className="text-slate-400 text-xs">até</span>
            <Input
              type="date"
              value={toDate}
              onChange={(e) => {
                const next = e.target.value
                setToDate(next)
                setPage(0)
                if (fromDate && next && fromDate > next) {
                  toast.error('Data inicial deve ser anterior à data final')
                }
              }}
              className="h-8 w-36 text-xs"
            />
          </div>

          <Select
            value={statusFilter}
            onValueChange={(v) => { setStatusFilter(v); setPage(0) }}
          >
            <SelectTrigger className="h-8 w-32 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Todos</SelectItem>
              <SelectItem value="OPEN">Abertos</SelectItem>
              <SelectItem value="CLOSED">Fechados</SelectItem>
            </SelectContent>
          </Select>

          {(fromDate || toDate || statusFilter !== 'all') && (
            <Button
              variant="ghost"
              size="sm"
              onClick={() => { setFromDate(''); setToDate(''); setStatusFilter('all'); setPage(0) }}
              className="text-xs text-slate-400"
            >
              Limpar filtros
            </Button>
          )}
        </div>

        <ShiftList data={data} isLoading={isLoading} />

        {/* Paginação */}
        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-between pt-2">
            <span className="text-xs text-slate-400">
              {data.totalElements} turnos · página {data.page + 1} de {data.totalPages}
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
