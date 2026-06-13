# Task 06 — Pista: sem turno aberto + OpenShiftSheet

**Files:**
- Create: `frontend/src/hooks/useShift.ts`
- Create: `frontend/src/components/pista/OpenShiftSheet.tsx`
- Modify: `frontend/src/pages/PistaPage.tsx`

Ao final desta task, a PistaPage exibe:
- Se posto não selecionado → aviso para selecionar posto
- Se posto selecionado + sem turno → tela centralizada com botão "Abrir turno"
- Clicar "Abrir turno" → OpenShiftSheet que registra encerrantes de abertura e abre turno

---

- [ ] **Step 1: Criar `frontend/src/hooks/useShift.ts`**

```typescript
import { useQuery } from '@tanstack/react-query'
import { useActiveStation } from './useActiveStation'
import { getOpenShift } from '@/api/shifts'

export function useShift() {
  const { station } = useActiveStation()

  return useQuery({
    queryKey: ['shift', 'open', station?.id],
    queryFn: () => getOpenShift(station!.id),
    enabled: !!station,
  })
}
```

- [ ] **Step 2: Criar `frontend/src/components/pista/OpenShiftSheet.tsx`**

O sheet abre turno em 2 chamadas sequenciais:
1. `POST /api/shifts` → retorna `{ id }` do turno aberto
2. Para cada bico: `POST /api/shifts/{id}/readings` com `type: 'OPENING'`

Se a segunda chamada falhar, exibe erro sem fechar o sheet.

```typescript
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient, useQueries } from '@tanstack/react-query'
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
import { useActiveStation } from '@/hooks/useActiveStation'
import { openShift } from '@/api/shifts'
import { registerReading } from '@/api/readings'
import { getPumps } from '@/api/pumps'
import { getNozzles } from '@/api/nozzles'
import { getFuels } from '@/api/fuels'

const schema = z.object({
  employeeName: z.string().min(1, 'Obrigatório').max(100),
  readings: z.record(z.string(), z.coerce.number().min(0, 'Obrigatório')),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function OpenShiftSheet({ open, onOpenChange }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()

  const { data: pumps = [] } = useQuery({
    queryKey: ['pumps', station?.id, 'ACTIVE'],
    queryFn: () => getPumps(station!.id, 'ACTIVE'),
    enabled: !!station && open,
  })

  const nozzleQueries = useQueries({
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

  const activeNozzles = nozzleQueries.flatMap((q) => q.data ?? [])
  const fuelById = Object.fromEntries(fuels.map((f) => [f.id, f]))

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(data: FormData) {
    if (!station) return
    setSubmitting(true)
    try {
      const shift = await openShift({
        stationId: station.id,
        employeeName: data.employeeName,
      })

      let readingError = false
      for (const nozzle of activeNozzles) {
        const totalizer = data.readings[nozzle.id]
        if (totalizer === undefined) continue
        try {
          await registerReading(shift.id, {
            nozzleId: nozzle.id,
            type: 'OPENING',
            totalizer,
          })
        } catch {
          toast.error(`Erro ao registrar encerrante do bico ${nozzle.number}`)
          readingError = true
        }
      }

      if (!readingError) {
        toast.success('Turno aberto!')
        qc.invalidateQueries({ queryKey: ['shift', 'open', station.id] })
        reset()
        onOpenChange(false)
      }
    } catch (err) {
      toast.error('Erro ao abrir turno')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[400px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>Abrir turno</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="mt-4 flex flex-col gap-4">
          <div>
            <Label htmlFor="employeeName">Nome do frentista</Label>
            <Input
              id="employeeName"
              placeholder="Ex: João Silva"
              {...register('employeeName')}
            />
            {errors.employeeName && (
              <p className="mt-1 text-xs text-red-500">{errors.employeeName.message}</p>
            )}
          </div>

          {activeNozzles.length > 0 && (
            <div>
              <p className="mb-2 text-sm font-semibold text-slate-700">
                Encerrantes de abertura
              </p>
              <div className="flex flex-col gap-3">
                {activeNozzles.map((nozzle) => (
                  <div key={nozzle.id}>
                    <Label htmlFor={`r-${nozzle.id}`}>
                      Bico {nozzle.number} — {fuelById[nozzle.fuelId]?.name ?? '…'}
                    </Label>
                    <Input
                      id={`r-${nozzle.id}`}
                      type="number"
                      step="0.001"
                      min="0"
                      placeholder="0.000"
                      {...register(`readings.${nozzle.id}`)}
                    />
                    {errors.readings?.[nozzle.id] && (
                      <p className="mt-1 text-xs text-red-500">
                        {errors.readings[nozzle.id]?.message}
                      </p>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}

          <Button type="submit" disabled={submitting} className="mt-2 bg-orange-600 hover:bg-orange-700">
            {submitting ? 'Abrindo…' : 'Abrir turno'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
```

- [ ] **Step 3: Atualizar `frontend/src/pages/PistaPage.tsx`**

```typescript
import { useState } from 'react'
import { Fuel } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { OpenShiftSheet } from '@/components/pista/OpenShiftSheet'
import { useActiveStation } from '@/hooks/useActiveStation'
import { useShift } from '@/hooks/useShift'

export function PistaPage() {
  const { station } = useActiveStation()
  const { data: shift, isLoading } = useShift()
  const [openSheetOpen, setOpenSheetOpen] = useState(false)

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

      <div className="flex flex-1 flex-col overflow-auto p-6">
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
          <div>
            {/* Turno aberto — métricas + bicos: Tasks 07 e 08 */}
            <p className="text-sm text-slate-400">
              Turno em andamento desde {new Date(shift.openedAt).toLocaleTimeString('pt-BR')}
            </p>
          </div>
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

- [ ] **Step 5: Teste manual (backend rodando)**

```bash
npm run dev
```

1. Navegue para `/pista`
2. Sem posto selecionado → deve mostrar mensagem de seleção
3. Com posto selecionado (via localStorage) → deve mostrar "Nenhum turno aberto" e botão
4. Clicar "Abrir turno" → sheet desliza com campo de frentista + inputs de encerrante por bico
5. Preencher e confirmar → toast "Turno aberto!" e sheet fecha

- [ ] **Step 6: Commit**

```bash
cd ..
git add frontend/src/hooks/useShift.ts frontend/src/components/pista/OpenShiftSheet.tsx frontend/src/pages/PistaPage.tsx
git commit -m "feat(frontend): pista sem turno e abertura de turno com encerrantes"
```
