# Task 08 — Pista: CloseShiftSheet (2 etapas)

**Files:**
- Create: `frontend/src/components/pista/CloseShiftSheet.tsx`
- Modify: `frontend/src/pages/PistaPage.tsx` (adicionar botão + sheet)

O sheet tem 2 etapas:
1. **Encerrantes de fechamento** — input por bico ativo, pré-preenchido com 0. Botão "Calcular reconciliação →" → POST readings CLOSING para cada bico → GET reconciliation.
2. **Reconciliação** — tabela por bico: medido / lançado / divergência com semáforo (verde=0, amarelo ≤0,6% do medido, vermelho >0,6%). Botão "Confirmar fechamento" → POST close.

---

- [ ] **Step 1: Criar `frontend/src/components/pista/CloseShiftSheet.tsx`**

```typescript
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueries, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'
import { formatLiters, formatBRL } from '@/lib/utils'
import { registerReading } from '@/api/readings'
import { closeShift, getReconciliation } from '@/api/shifts'
import { getPumps } from '@/api/pumps'
import { getNozzles } from '@/api/nozzles'
import { getFuels } from '@/api/fuels'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { ShiftReconciliation, ReconciliationLine } from '@/types'

const schema = z.object({
  readings: z.record(z.string(), z.coerce.number().min(0, 'Obrigatório')),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  shiftId: string
  employeeName: string
}

function divergenceBadge(line: ReconciliationLine) {
  const div = Math.abs(line.divergenceLiters)
  if (div === 0) {
    return <Badge className="bg-green-100 text-green-700 hover:bg-green-100">0 L</Badge>
  }
  const pct = line.measuredLiters > 0 ? (div / line.measuredLiters) * 100 : 0
  if (pct <= 0.6) {
    return (
      <Badge className="bg-yellow-100 text-yellow-700 hover:bg-yellow-100">
        {formatLiters(div)} ({pct.toFixed(2)}%)
      </Badge>
    )
  }
  return (
    <Badge className="bg-red-100 text-red-700 hover:bg-red-100">
      {formatLiters(div)} ({pct.toFixed(2)}%)
    </Badge>
  )
}

export function CloseShiftSheet({ open, onOpenChange, shiftId, employeeName }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const [step, setStep] = useState<1 | 2>(1)
  const [reconciliation, setReconciliation] = useState<ShiftReconciliation | null>(null)
  const [calculating, setCalculating] = useState(false)

  const { data: pumps = [] } = useQuery({
    queryKey: ['pumps', station?.id, 'ACTIVE'],
    queryFn: () => getPumps(station!.id, 'ACTIVE'),
    enabled: !!station && open,
  })

  const nozzleResults = useQueries({
    queries: pumps.map((pump) => ({
      queryKey: ['nozzles', pump.id, 'active'],
      queryFn: () => getNozzles(pump.id, true),
      enabled: pumps.length > 0 && open,
    })),
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
    enabled: open,
  })

  const activeNozzles = nozzleResults.flatMap((q) => q.data ?? [])
  const fuelById = Object.fromEntries(fuels.map((f) => [f.id, f]))

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema),
  })

  async function onCalculate(data: FormData) {
    setCalculating(true)
    try {
      for (const nozzle of activeNozzles) {
        const totalizer = data.readings[nozzle.id]
        if (totalizer === undefined) continue
        await registerReading(shiftId, {
          nozzleId: nozzle.id,
          type: 'CLOSING',
          totalizer,
        })
      }
      const rec = await getReconciliation(shiftId)
      setReconciliation(rec)
      setStep(2)
    } catch {
      toast.error('Erro ao registrar encerrantes de fechamento')
    } finally {
      setCalculating(false)
    }
  }

  const closeMutation = useMutation({
    mutationFn: () => closeShift(shiftId),
    onSuccess: () => {
      toast.success('Turno fechado!')
      qc.invalidateQueries({ queryKey: ['shift', 'open', station?.id] })
      onOpenChange(false)
      setStep(1)
      setReconciliation(null)
    },
    onError: () => {
      toast.error('Erro ao fechar turno')
    },
  })

  function handleClose() {
    onOpenChange(false)
    setStep(1)
    setReconciliation(null)
  }

  return (
    <Sheet open={open} onOpenChange={handleClose}>
      <SheetContent className="w-[480px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>
            Fechar turno — {employeeName}
          </SheetTitle>
        </SheetHeader>

        {/* Indicador de etapa */}
        <div className="mt-3 mb-4 flex gap-2">
          <span className={cn('rounded-full px-3 py-1 text-xs font-semibold', step === 1 ? 'bg-orange-600 text-white' : 'bg-slate-100 text-slate-400')}>
            1. Encerrantes
          </span>
          <span className={cn('rounded-full px-3 py-1 text-xs font-semibold', step === 2 ? 'bg-orange-600 text-white' : 'bg-slate-100 text-slate-400')}>
            2. Reconciliação
          </span>
        </div>

        {/* Etapa 1: encerrantes */}
        {step === 1 && (
          <form onSubmit={handleSubmit(onCalculate)} className="flex flex-col gap-4">
            <p className="text-sm text-slate-500">
              Informe a leitura do encerrante de fechamento para cada bico.
            </p>
            {activeNozzles.map((nozzle) => (
              <div key={nozzle.id}>
                <Label htmlFor={`c-${nozzle.id}`}>
                  Bico {nozzle.number} — {fuelById[nozzle.fuelId]?.name ?? '…'}
                </Label>
                <Input
                  id={`c-${nozzle.id}`}
                  type="number"
                  step="0.001"
                  min="0"
                  placeholder="0.000"
                  defaultValue="0"
                  {...register(`readings.${nozzle.id}`)}
                />
                {errors.readings?.[nozzle.id] && (
                  <p className="mt-1 text-xs text-red-500">
                    {errors.readings[nozzle.id]?.message}
                  </p>
                )}
              </div>
            ))}
            <Button
              type="submit"
              disabled={calculating}
              className="mt-2 bg-orange-600 hover:bg-orange-700"
            >
              {calculating ? 'Calculando…' : 'Calcular reconciliação →'}
            </Button>
          </form>
        )}

        {/* Etapa 2: reconciliação */}
        {step === 2 && reconciliation && (
          <div className="flex flex-col gap-4">
            <p className="text-sm text-slate-500">
              Revise a reconciliação antes de confirmar o fechamento.
            </p>

            <div className="overflow-hidden rounded-lg border">
              <table className="w-full text-xs">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-3 py-2 text-left font-semibold text-slate-500">Bico</th>
                    <th className="px-3 py-2 text-right font-semibold text-slate-500">Medido</th>
                    <th className="px-3 py-2 text-right font-semibold text-slate-500">Lançado</th>
                    <th className="px-3 py-2 text-right font-semibold text-slate-500">Divergência</th>
                  </tr>
                </thead>
                <tbody>
                  {reconciliation.lines.map((line) => (
                    <tr key={line.nozzleId} className="border-t">
                      <td className="px-3 py-2 font-medium">
                        B{line.nozzleNumber} · {line.fuelName}
                      </td>
                      <td className="px-3 py-2 text-right tabular-nums">
                        {formatLiters(line.measuredLiters)}
                      </td>
                      <td className="px-3 py-2 text-right tabular-nums">
                        {formatLiters(line.fueledLiters)}
                      </td>
                      <td className="px-3 py-2 text-right">
                        {divergenceBadge(line)}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="border-t bg-slate-50">
                  <tr>
                    <td className="px-3 py-2 font-semibold" colSpan={2}>Total medido</td>
                    <td className="px-3 py-2 text-right font-semibold tabular-nums" colSpan={2}>
                      {formatLiters(reconciliation.totalMeasuredLiters)}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>

            <div className="flex gap-2">
              <Button
                variant="outline"
                onClick={() => setStep(1)}
                className="flex-1"
              >
                ← Voltar
              </Button>
              <Button
                onClick={() => closeMutation.mutate()}
                disabled={closeMutation.isPending}
                className="flex-1 bg-red-600 hover:bg-red-700"
              >
                {closeMutation.isPending ? 'Fechando…' : 'Confirmar fechamento'}
              </Button>
            </div>
          </div>
        )}
      </SheetContent>
    </Sheet>
  )
}
```

- [ ] **Step 2: Atualizar `frontend/src/pages/PistaPage.tsx`**

Adicionar o botão "Fechar turno" e o sheet. Substituir o comentário `{/* Fechar turno: Task 08 */}` na seção de `actions` do TopBar:

```typescript
// Adicionar ao imports:
import { CloseShiftSheet } from '@/components/pista/CloseShiftSheet'

// Adicionar estado:
const [closeSheetOpen, setCloseSheetOpen] = useState(false)

// Substituir o comentário no actions do TopBar:
<>
  <span className="rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700">
    ● Turno aberto · {shift.employeeName}
  </span>
  <Button
    size="sm"
    variant="destructive"
    onClick={() => setCloseSheetOpen(true)}
  >
    Fechar turno
  </Button>
</>

// Adicionar antes do final do return (junto com OpenShiftSheet):
<CloseShiftSheet
  open={closeSheetOpen}
  onOpenChange={setCloseSheetOpen}
  shiftId={shift.id}
  employeeName={shift.employeeName}
/>
```

O arquivo completo de `PistaPage.tsx` após a edição:

```typescript
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Fuel } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { OpenShiftSheet } from '@/components/pista/OpenShiftSheet'
import { CloseShiftSheet } from '@/components/pista/CloseShiftSheet'
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
  const [closeSheetOpen, setCloseSheetOpen] = useState(false)

  const { data: summary } = useQuery({
    queryKey: ['shift-summary', shift?.id],
    queryFn: () => getShiftSummary(shift!.id),
    enabled: !!shift,
    refetchInterval: 30_000,
  })

  const lastFueledAt =
    summary?.fuelings.length
      ? new Date(Math.max(...summary.fuelings.map((f) => new Date(f.fueledAt).getTime())))
      : null

  function relativeTime(date: Date): string {
    const diffMin = Math.floor((Date.now() - date.getTime()) / 60_000)
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
            <>
              <span className="rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700">
                ● Turno aberto · {shift.employeeName}
              </span>
              <Button size="sm" variant="destructive" onClick={() => setCloseSheetOpen(true)}>
                Fechar turno
              </Button>
            </>
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
            <Button onClick={() => setOpenSheetOpen(true)} className="bg-orange-600 hover:bg-orange-700">
              Abrir turno
            </Button>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-4 gap-3">
              <MetricCard label="Volume total" value={formatLiters(summary?.totalLiters ?? 0)} />
              <MetricCard label="Receita" value={formatBRL(summary?.totalAmount ?? 0)} />
              <MetricCard label="Abastecimentos" value={String(summary?.fuelings.length ?? 0)} />
              <MetricCard label="Últ. abastecimento" value={lastFueledAt ? relativeTime(lastFueledAt) : '—'} />
            </div>
            <NozzleList shiftId={shift.id} summary={summary} />
          </>
        )}
      </div>

      <OpenShiftSheet open={openSheetOpen} onOpenChange={setOpenSheetOpen} />
      {shift && (
        <CloseShiftSheet
          open={closeSheetOpen}
          onOpenChange={setCloseSheetOpen}
          shiftId={shift.id}
          employeeName={shift.employeeName}
        />
      )}
    </div>
  )
}
```

- [ ] **Step 3: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros TypeScript.

- [ ] **Step 4: Teste manual (backend rodando com turno aberto)**

1. Com turno aberto, clicar "Fechar turno"
2. Sheet etapa 1: inputs de encerrante por bico
3. Preencher encerrantes e clicar "Calcular reconciliação →"
4. Sheet etapa 2: tabela de reconciliação com badges coloridos
5. Divergência 0 → verde; ≤0,6% → amarelo; >0,6% → vermelho
6. Clicar "Confirmar fechamento" → toast "Turno fechado!" + PistaPage volta para estado sem turno

- [ ] **Step 5: Commit**

```bash
cd ..
git add frontend/src/components/pista/CloseShiftSheet.tsx frontend/src/pages/PistaPage.tsx
git commit -m "feat(frontend): fechamento de turno com encerrantes e reconciliação"
```
