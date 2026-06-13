# Task 07 — Pista: turno aberto — métricas + NozzleList + FuelingForm

**Files:**
- Create: `frontend/src/components/pista/NozzleList.tsx`
- Create: `frontend/src/components/pista/FuelingForm.tsx`
- Modify: `frontend/src/pages/PistaPage.tsx`

Ao final desta task, quando há turno aberto:
- 4 cards de métricas no topo (volume, receita, qtd, último abastecimento)
- Lista de bicos agrupados por bomba, cada um com subtotais do turno
- Botão "+ Registrar" por bico expande um form inline (só 1 expandido por vez)
- Confirmar abastecimento → toast + invalida queries → métricas atualizam

---

- [ ] **Step 1: Criar `frontend/src/components/pista/FuelingForm.tsx`**

```typescript
import { useEffect } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { cn } from '@/lib/utils'
import { formatBRL } from '@/lib/utils'
import { registerFueling } from '@/api/fuelings'
import { getCurrentPrices } from '@/api/prices'
import { useActiveStation } from '@/hooks/useActiveStation'
import { PAYMENT_METHOD_LABELS, type PaymentMethod } from '@/types'

const PAYMENT_METHODS = Object.entries(PAYMENT_METHOD_LABELS) as [PaymentMethod, string][]

const schema = z.object({
  liters: z.coerce.number({ invalid_type_error: 'Obrigatório' }).positive('Deve ser maior que 0'),
  paymentMethod: z.string().min(1, 'Selecione uma forma de pagamento'),
  vehiclePlate: z.string().max(10).optional(),
})

type FormData = z.infer<typeof schema>

type Props = {
  shiftId: string
  nozzleId: string
  nozzleNumber: number
  fuelName: string
  onClose: () => void
}

export function FuelingForm({ shiftId, nozzleId, nozzleNumber, fuelName, onClose }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()

  const { data: prices = [] } = useQuery({
    queryKey: ['prices', station?.id],
    queryFn: () => getCurrentPrices(station!.id),
    enabled: !!station,
  })

  const currentPrice = prices.find((p) =>
    // FuelPrice não tem nozzleId — mas temos fuelName para correlacionar
    // Alternativa: buscar fuelId via nozzle query (já feita no NozzleList)
    // Por simplicidade, recebemos fuelName como prop e filtramos por ele
    p.fuelName === fuelName,
  )?.price ?? 0

  const {
    register,
    handleSubmit,
    watch,
    control,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { paymentMethod: '' },
  })

  const liters = watch('liters')
  const totalAmount = liters > 0 && currentPrice > 0 ? liters * currentPrice : 0

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      registerFueling(shiftId, {
        nozzleId,
        liters: data.liters,
        totalAmount,
        paymentMethod: data.paymentMethod,
        vehiclePlate: data.vehiclePlate || undefined,
      }),
    onSuccess: () => {
      toast.success('Abastecimento registrado!')
      qc.invalidateQueries({ queryKey: ['shift-summary', shiftId] })
      reset()
      onClose()
    },
    onError: () => {
      toast.error('Erro ao registrar abastecimento')
    },
  })

  return (
    <div className="border-t border-orange-200 bg-orange-50/50 px-4 py-3">
      <div className="mb-2 flex items-center justify-between">
        <span className="text-xs font-semibold text-orange-600">
          Bico {nozzleNumber} · {fuelName}
          {currentPrice > 0 && (
            <span className="ml-1 font-normal text-orange-400">
              · R$ {currentPrice.toFixed(3)}/L
            </span>
          )}
        </span>
        <button onClick={onClose} className="text-slate-400 hover:text-slate-600">
          <X size={14} />
        </button>
      </div>

      <form
        onSubmit={handleSubmit((d) => mutation.mutate(d))}
        className="flex flex-wrap items-end gap-3"
      >
        {/* Litros */}
        <div className="min-w-[90px]">
          <Label className="text-[10px]">LITROS</Label>
          <Input
            type="number"
            step="0.001"
            min="0.001"
            placeholder="0.000"
            className="font-semibold"
            {...register('liters')}
          />
          {errors.liters && (
            <p className="mt-0.5 text-[10px] text-red-500">{errors.liters.message}</p>
          )}
        </div>

        {/* Total calculado */}
        <div className="min-w-[90px]">
          <Label className="text-[10px]">TOTAL (calc.)</Label>
          <div
            className={cn(
              'flex h-9 items-center rounded-md border px-3 text-sm font-semibold',
              totalAmount > 0
                ? 'border-green-300 bg-green-50 text-green-700'
                : 'border-slate-200 bg-slate-50 text-slate-400',
            )}
          >
            {totalAmount > 0 ? formatBRL(totalAmount) : '—'}
          </div>
        </div>

        {/* Pagamento */}
        <div>
          <Label className="text-[10px]">PAGAMENTO</Label>
          <Controller
            control={control}
            name="paymentMethod"
            render={({ field }) => (
              <div className="flex flex-wrap gap-1">
                {PAYMENT_METHODS.map(([value, label]) => (
                  <button
                    key={value}
                    type="button"
                    onClick={() => field.onChange(value)}
                    className={cn(
                      'rounded border px-2 py-1 text-[10px] font-semibold',
                      field.value === value
                        ? 'border-orange-600 bg-orange-50 text-orange-600'
                        : 'border-slate-200 bg-white text-slate-500 hover:border-slate-300',
                    )}
                  >
                    {label}
                  </button>
                ))}
              </div>
            )}
          />
          {errors.paymentMethod && (
            <p className="mt-0.5 text-[10px] text-red-500">{errors.paymentMethod.message}</p>
          )}
        </div>

        {/* Placa (opcional) */}
        <div className="min-w-[80px]">
          <Label className="text-[10px]">PLACA (opc.)</Label>
          <Input
            placeholder="ABC-1234"
            className="uppercase"
            maxLength={10}
            {...register('vehiclePlate')}
          />
        </div>

        <Button
          type="submit"
          disabled={mutation.isPending}
          className="bg-green-600 hover:bg-green-700"
        >
          {mutation.isPending ? '…' : '✓ Confirmar'}
        </Button>
      </form>
    </div>
  )
}
```

- [ ] **Step 2: Criar `frontend/src/components/pista/NozzleList.tsx`**

```typescript
import { useState } from 'react'
import { useQuery, useQueries, useQueryClient } from '@tanstack/react-query'
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
          <div key={pump.id} className="overflow-hidden rounded-lg border bg-white">
            <div className="border-b bg-slate-50 px-4 py-2 text-xs font-semibold text-slate-500">
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
                      <span className="rounded bg-slate-100 px-2 py-0.5 text-xs font-bold text-slate-500">
                        B{nozzle.number}
                      </span>
                      <div>
                        <p className="text-sm font-semibold text-slate-800">
                          {fuel?.name ?? '—'}
                        </p>
                        <p className="text-xs text-slate-400">
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
```

- [ ] **Step 3: Atualizar `frontend/src/pages/PistaPage.tsx`**

Adiciona cards de métricas e NozzleList quando turno está aberto:

```typescript
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
```

- [ ] **Step 4: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros TypeScript.

- [ ] **Step 5: Teste manual (backend rodando com turno aberto)**

1. Com turno aberto, navegar para `/pista`
2. 4 cards de métricas devem aparecer (zeros se sem abastecimentos)
3. Lista de bicos agrupada por bomba
4. Clicar "+ Registrar" em um bico → form expande inline
5. Clicar em outro bico → form anterior fecha, novo abre
6. Preencher litros + pagamento + confirmar → toast "Abastecimento registrado!" + métricas atualizam

- [ ] **Step 6: Commit**

```bash
cd ..
git add frontend/src/components/pista/FuelingForm.tsx frontend/src/components/pista/NozzleList.tsx frontend/src/pages/PistaPage.tsx
git commit -m "feat(frontend): pista com turno aberto — métricas, bicos e registro de abastecimento"
```
