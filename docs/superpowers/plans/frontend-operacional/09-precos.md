# Task 09 — Preços: PriceTable + NewPriceForm + PriceHistoryModal

**Files:**
- Create: `frontend/src/components/precos/PriceTable.tsx`
- Create: `frontend/src/components/precos/NewPriceForm.tsx`
- Create: `frontend/src/components/precos/PriceHistoryModal.tsx`
- Modify: `frontend/src/pages/PrecosPage.tsx`

Layout: 2 painéis lado a lado.
- Esquerda: tabela de preços vigentes com link "Ver ›" por combustível
- Direita: mini-form para atualizar preço com prévia de variação
- Clicar "Ver ›" abre PriceHistoryModal com histórico cronológico

---

- [ ] **Step 1: Criar `frontend/src/components/precos/PriceHistoryModal.tsx`**

```typescript
import { useQuery } from '@tanstack/react-query'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { getPriceHistory } from '@/api/prices'
import { useActiveStation } from '@/hooks/useActiveStation'
import { formatBRL } from '@/lib/utils'

type Props = {
  fuelId: string
  fuelName: string
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function PriceHistoryModal({ fuelId, fuelName, open, onOpenChange }: Props) {
  const { station } = useActiveStation()

  const { data: history = [], isLoading } = useQuery({
    queryKey: ['price-history', station?.id, fuelId],
    queryFn: () => getPriceHistory(station!.id, fuelId),
    enabled: !!station && open,
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Histórico de preços — {fuelName}</DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : history.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum histórico disponível.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border">
            <table className="w-full text-sm">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">
                    Preço (R$/L)
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">
                    Vigente desde
                  </th>
                </tr>
              </thead>
              <tbody>
                {history.map((entry, idx) => (
                  <tr key={entry.id} className="border-t">
                    <td className="px-4 py-2 font-semibold text-orange-600 tabular-nums">
                      {formatBRL(entry.price)}
                      {idx === 0 && (
                        <span className="ml-2 rounded-full bg-orange-100 px-2 py-0.5 text-[10px] font-semibold text-orange-600">
                          atual
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-2 text-slate-500">
                      {new Date(entry.effectiveFrom).toLocaleDateString('pt-BR', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
```

- [ ] **Step 2: Criar `frontend/src/components/precos/PriceTable.tsx`**

```typescript
import { useState } from 'react'
import { Skeleton } from '@/components/ui/skeleton'
import { PriceHistoryModal } from './PriceHistoryModal'
import { formatBRL } from '@/lib/utils'
import type { FuelPrice } from '@/types'

type Props = {
  prices: FuelPrice[]
  isLoading: boolean
}

export function PriceTable({ prices, isLoading }: Props) {
  const [historyModal, setHistoryModal] = useState<{ fuelId: string; fuelName: string } | null>(
    null,
  )

  if (isLoading) {
    return (
      <div className="flex flex-col gap-2">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-10 w-full" />
      </div>
    )
  }

  if (prices.length === 0) {
    return <p className="text-sm text-slate-400">Nenhum preço cadastrado.</p>
  }

  return (
    <>
      <div className="overflow-hidden rounded-lg border bg-white">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-slate-50">
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">
                Combustível
              </th>
              <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400">
                Preço/L
              </th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">
                Desde
              </th>
              <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400">
                Histórico
              </th>
            </tr>
          </thead>
          <tbody>
            {prices.map((p) => (
              <tr key={p.id} className="border-b last:border-0">
                <td className="px-4 py-2.5 font-semibold text-slate-800">{p.fuelName}</td>
                <td className="px-4 py-2.5 text-right font-bold text-orange-600 tabular-nums">
                  {formatBRL(p.price)}
                </td>
                <td className="px-4 py-2.5 text-slate-400">
                  {new Date(p.effectiveFrom).toLocaleDateString('pt-BR')}
                </td>
                <td className="px-4 py-2.5 text-center">
                  <button
                    onClick={() => setHistoryModal({ fuelId: p.fuelId, fuelName: p.fuelName })}
                    className="text-xs font-medium text-blue-500 hover:text-blue-700"
                  >
                    Ver ›
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {historyModal && (
        <PriceHistoryModal
          fuelId={historyModal.fuelId}
          fuelName={historyModal.fuelName}
          open={!!historyModal}
          onOpenChange={(open) => !open && setHistoryModal(null)}
        />
      )}
    </>
  )
}
```

- [ ] **Step 3: Criar `frontend/src/components/precos/NewPriceForm.tsx`**

```typescript
import { useEffect } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { formatBRL } from '@/lib/utils'
import { createPrice } from '@/api/prices'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { FuelPrice, Fuel } from '@/types'

const schema = z.object({
  fuelId: z.string().min(1, 'Selecione um combustível'),
  price: z.coerce
    .number({ invalid_type_error: 'Obrigatório' })
    .positive('Deve ser maior que 0'),
})

type FormData = z.infer<typeof schema>

type Props = {
  fuels: Fuel[]
  currentPrices: FuelPrice[]
}

export function NewPriceForm({ fuels, currentPrices }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()

  const {
    register,
    handleSubmit,
    watch,
    control,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const selectedFuelId = watch('fuelId')
  const newPrice = watch('price')

  const currentPrice = currentPrices.find((p) => p.fuelId === selectedFuelId)?.price
  const variation =
    currentPrice && newPrice > 0 ? newPrice - currentPrice : null
  const variationPct =
    currentPrice && variation !== null ? (variation / currentPrice) * 100 : null

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      createPrice(station!.id, { fuelId: data.fuelId, price: data.price }),
    onSuccess: () => {
      toast.success('Preço atualizado!')
      qc.invalidateQueries({ queryKey: ['prices', station?.id] })
      reset()
    },
    onError: () => {
      toast.error('Erro ao atualizar preço')
    },
  })

  return (
    <form
      onSubmit={handleSubmit((d) => mutation.mutate(d))}
      className="flex flex-col gap-4"
    >
      <h2 className="text-sm font-bold text-slate-800">Atualizar preço</h2>

      <div>
        <Label>Combustível</Label>
        <Controller
          control={control}
          name="fuelId"
          render={({ field }) => (
            <Select onValueChange={field.onChange} value={field.value}>
              <SelectTrigger>
                <SelectValue placeholder="Selecionar…" />
              </SelectTrigger>
              <SelectContent>
                {fuels.filter((f) => f.active).map((fuel) => (
                  <SelectItem key={fuel.id} value={fuel.id}>
                    {fuel.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        />
        {errors.fuelId && (
          <p className="mt-1 text-xs text-red-500">{errors.fuelId.message}</p>
        )}
      </div>

      <div>
        <Label>Novo preço (R$/L)</Label>
        <Input
          type="number"
          step="0.001"
          min="0.001"
          placeholder="0.000"
          className="text-lg font-bold text-orange-600"
          {...register('price')}
        />
        {errors.price && (
          <p className="mt-1 text-xs text-red-500">{errors.price.message}</p>
        )}
      </div>

      {/* Prévia de variação */}
      {currentPrice !== undefined && variation !== null && variationPct !== null && (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3">
          <p className="text-xs text-amber-700">Preço anterior: {formatBRL(currentPrice)}</p>
          <p className="text-xs font-semibold text-amber-900">
            Variação:{' '}
            {variation >= 0 ? '+' : ''}
            {formatBRL(variation)} ({variationPct >= 0 ? '+' : ''}
            {variationPct.toFixed(1)}%)
          </p>
        </div>
      )}

      <Button
        type="submit"
        disabled={mutation.isPending || !station}
        className="bg-orange-600 hover:bg-orange-700"
      >
        {mutation.isPending ? 'Salvando…' : 'Confirmar preço'}
      </Button>
    </form>
  )
}
```

- [ ] **Step 4: Atualizar `frontend/src/pages/PrecosPage.tsx`**

```typescript
import { useQuery } from '@tanstack/react-query'
import { TopBar } from '@/components/layout/TopBar'
import { PriceTable } from '@/components/precos/PriceTable'
import { NewPriceForm } from '@/components/precos/NewPriceForm'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getCurrentPrices } from '@/api/prices'
import { getFuels } from '@/api/fuels'

export function PrecosPage() {
  const { station } = useActiveStation()

  const { data: prices = [], isLoading: pricesLoading } = useQuery({
    queryKey: ['prices', station?.id],
    queryFn: () => getCurrentPrices(station!.id),
    enabled: !!station,
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
  })

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center p-6 text-slate-400 text-sm">
        Selecione um posto para ver os preços.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Preços" />

      <div className="flex flex-1 gap-0 overflow-hidden">
        {/* Tabela de preços vigentes */}
        <div className="flex-1 overflow-auto p-6">
          <h2 className="mb-3 text-sm font-bold text-slate-800">Preços vigentes</h2>
          <PriceTable prices={prices} isLoading={pricesLoading} />
        </div>

        {/* Mini-form novo preço */}
        <div className="w-56 shrink-0 overflow-auto border-l bg-white p-4">
          <NewPriceForm fuels={fuels} currentPrices={prices} />
        </div>
      </div>
    </div>
  )
}
```

- [ ] **Step 5: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros TypeScript.

- [ ] **Step 6: Teste manual (backend rodando)**

1. Navegar para `/precos`
2. Tabela de preços vigentes deve aparecer (se cadastrados)
3. Clicar "Ver ›" → modal de histórico abre com tabela cronológica
4. No painel direito: selecionar combustível + digitar novo preço
5. Prévia de variação deve aparecer em tempo real
6. Confirmar → toast "Preço atualizado!" + tabela atualiza

- [ ] **Step 7: Commit**

```bash
cd ..
git add frontend/src/components/precos/ frontend/src/pages/PrecosPage.tsx
git commit -m "feat(frontend): módulo de preços — tabela vigente, histórico e atualização"
```
