# Task 13 — Histórico: ShiftList + ShiftDetailModal + HistoricoPage

**Files:**
- Create: `frontend/src/components/historico/ShiftDetailModal.tsx`
- Create: `frontend/src/components/historico/ShiftList.tsx`
- Modify: `frontend/src/pages/HistoricoPage.tsx`

Lista paginada de turnos do posto ativo com filtros (datas + status). Clicar numa linha abre modal com resumo, abastecimentos e reconciliação.

**Nota:** `ShiftResponse` não inclui volume/receita. Colunas da lista: Frentista / Abertura / Fechamento / Duração / Status. Os totais ficam no modal.

---

- [ ] **Step 1: Criar `frontend/src/components/historico/ShiftDetailModal.tsx`**

```typescript
import { useQuery } from '@tanstack/react-query'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { Separator } from '@/components/ui/separator'
import { getShiftSummary } from '@/api/fuelings'
import { getReconciliation } from '@/api/shifts'
import { formatBRL, formatLiters } from '@/lib/utils'
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
  const { data: summary, isLoading: summaryLoading } = useQuery({
    queryKey: ['shift-summary', shift.id],
    queryFn: () => getShiftSummary(shift.id),
    enabled: open,
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
            <span className="text-slate-400">Abertura:</span>{' '}
            <span className="font-medium">
              {new Date(shift.openedAt).toLocaleString('pt-BR')}
            </span>
          </div>
          {shift.closedAt && (
            <div>
              <span className="text-slate-400">Fechamento:</span>{' '}
              <span className="font-medium">
                {new Date(shift.closedAt).toLocaleString('pt-BR')}
              </span>
            </div>
          )}
          {duration !== null && (
            <div>
              <span className="text-slate-400">Duração:</span>{' '}
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
            <div className="flex gap-4 rounded-lg border bg-slate-50 p-3 text-sm">
              <div>
                <p className="text-[10px] uppercase text-slate-400">Volume</p>
                <p className="font-bold">{formatLiters(summary.totalLiters)}</p>
              </div>
              <div>
                <p className="text-[10px] uppercase text-slate-400">Receita</p>
                <p className="font-bold text-orange-600">{formatBRL(summary.totalAmount)}</p>
              </div>
              <div>
                <p className="text-[10px] uppercase text-slate-400">Abastecimentos</p>
                <p className="font-bold">{summary.fuelings.length}</p>
              </div>
            </div>

            <Separator />

            {/* Abastecimentos */}
            <div>
              <p className="mb-2 text-sm font-semibold text-slate-700">Abastecimentos</p>
              {summary.fuelings.length === 0 ? (
                <p className="text-xs text-slate-400">Nenhum abastecimento neste turno.</p>
              ) : (
                <div className="overflow-hidden rounded-lg border">
                  <table className="w-full text-xs">
                    <thead className="bg-slate-50">
                      <tr>
                        <th className="px-3 py-2 text-left font-semibold text-slate-400">Horário</th>
                        <th className="px-3 py-2 text-left font-semibold text-slate-400">Bico</th>
                        <th className="px-3 py-2 text-left font-semibold text-slate-400">Combustível</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-400">Litros</th>
                        <th className="px-3 py-2 text-right font-semibold text-slate-400">Total</th>
                        <th className="px-3 py-2 text-left font-semibold text-slate-400">Pgto</th>
                      </tr>
                    </thead>
                    <tbody>
                      {summary.fuelings.map((f) => (
                        <tr key={f.id} className="border-t">
                          <td className="px-3 py-1.5 text-slate-400">
                            {new Date(f.fueledAt).toLocaleTimeString('pt-BR', {
                              hour: '2-digit',
                              minute: '2-digit',
                            })}
                          </td>
                          <td className="px-3 py-1.5 font-medium">B{f.nozzleNumber}</td>
                          <td className="px-3 py-1.5 text-slate-600">{f.fuelName}</td>
                          <td className="px-3 py-1.5 text-right tabular-nums">
                            {formatLiters(f.liters)}
                          </td>
                          <td className="px-3 py-1.5 text-right font-semibold tabular-nums text-orange-600">
                            {formatBRL(f.totalAmount)}
                          </td>
                          <td className="px-3 py-1.5 text-slate-400">
                            {PAYMENT_METHOD_LABELS[f.paymentMethod as PaymentMethod] ?? f.paymentMethod}
                          </td>
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
                  <p className="mb-2 text-sm font-semibold text-slate-700">Reconciliação</p>
                  <div className="overflow-hidden rounded-lg border">
                    <table className="w-full text-xs">
                      <thead className="bg-slate-50">
                        <tr>
                          <th className="px-3 py-2 text-left font-semibold text-slate-400">Bico</th>
                          <th className="px-3 py-2 text-right font-semibold text-slate-400">Medido</th>
                          <th className="px-3 py-2 text-right font-semibold text-slate-400">Lançado</th>
                          <th className="px-3 py-2 text-right font-semibold text-slate-400">Divergência</th>
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
                      <tfoot className="border-t bg-slate-50">
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
                </div>
              </>
            )}
          </>
        )}
      </DialogContent>
    </Dialog>
  )
}
```

- [ ] **Step 2: Criar `frontend/src/components/historico/ShiftList.tsx`**

```typescript
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
```

- [ ] **Step 3: Atualizar `frontend/src/pages/HistoricoPage.tsx`**

```typescript
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
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
    enabled: !!station,
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
```

- [ ] **Step 4: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros TypeScript.

- [ ] **Step 5: Teste manual (backend rodando)**

1. Navegar para `/historico`
2. Lista paginada de turnos (10 por página)
3. Filtros de data e status funcionam
4. Clicar numa linha → modal abre com resumo do turno
5. Modal mostra: horários, totais de volume e receita, tabela de abastecimentos
6. Se turno fechado: seção de reconciliação com semáforo de divergência

- [ ] **Step 6: Commit**

```bash
cd ..
git add frontend/src/components/historico/ frontend/src/pages/HistoricoPage.tsx
git commit -m "feat(frontend): histórico de turnos com filtros e modal de detalhe"
```

---

## Frontend completo!

Após esta task, todas as rotas estão implementadas:

| Rota | Status |
|------|--------|
| `/pista` | ✅ Abrir turno + encerrantes + bicos + fuelings + fechar turno |
| `/precos` | ✅ Tabela vigente + histórico + atualizar preço |
| `/cadastros/postos` | ✅ CRUD + toggle status |
| `/cadastros/bombas` | ✅ CRUD + select de status |
| `/cadastros/bicos` | ✅ CRUD + toggle status + filtro bomba |
| `/cadastros/combustiveis` | ✅ Toggle status |
| `/historico` | ✅ Lista paginada + filtros + modal de detalhe |

Build final de verificação:

```bash
cd frontend && npm run build
```
