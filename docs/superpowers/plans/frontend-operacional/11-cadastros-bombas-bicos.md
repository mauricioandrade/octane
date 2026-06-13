# Task 11 — Cadastros: BombasPage + NozzleSheet + BicosPage

**Files:**
- Create: `frontend/src/components/cadastros/PumpSheet.tsx`
- Modify: `frontend/src/pages/BombasPage.tsx`
- Create: `frontend/src/components/cadastros/NozzleSheet.tsx`
- Modify: `frontend/src/pages/BicosPage.tsx`

Bombas e Bicos são contextuais ao posto ativo.
- Bombas: dropdown de status (ACTIVE/INACTIVE/MAINTENANCE) inline; editar via sheet (só o número).
- Bicos: select de bomba (das ativas) + combustível (dos ativos) no sheet.

---

- [ ] **Step 1: Criar `frontend/src/components/cadastros/PumpSheet.tsx`**

```typescript
import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
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
import { createPump, updatePump } from '@/api/pumps'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { Pump } from '@/types'

const schema = z.object({
  number: z.coerce.number({ invalid_type_error: 'Obrigatório' }).int().min(1),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  pump?: Pump
}

export function PumpSheet({ open, onOpenChange, pump }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const isEdit = !!pump

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (open) reset({ number: pump?.number ?? undefined })
  }, [open, pump, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? updatePump(pump.id, { number: data.number })
        : createPump(station!.id, { number: data.number }),
    onSuccess: () => {
      toast.success(isEdit ? 'Bomba atualizada!' : 'Bomba criada!')
      qc.invalidateQueries({ queryKey: ['pumps', station?.id] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error(isEdit ? 'Erro ao atualizar bomba' : 'Erro ao criar bomba')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[320px]">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar bomba' : 'Nova bomba'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Número da bomba</Label>
            <Input type="number" min="1" placeholder="1" {...register('number')} />
            {errors.number && (
              <p className="mt-1 text-xs text-red-500">{errors.number.message}</p>
            )}
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending || !station}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar' : 'Criar bomba'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
```

- [ ] **Step 2: Atualizar `frontend/src/pages/BombasPage.tsx`**

```typescript
import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Pencil, Plus } from 'lucide-react'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { Badge } from '@/components/ui/badge'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { TopBar } from '@/components/layout/TopBar'
import { PumpSheet } from '@/components/cadastros/PumpSheet'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getPumps, patchPumpStatus } from '@/api/pumps'
import type { Pump, PumpStatus } from '@/types'

const STATUS_LABELS: Record<PumpStatus, string> = {
  ACTIVE: 'Ativa',
  INACTIVE: 'Inativa',
  MAINTENANCE: 'Manutenção',
}

const STATUS_COLORS: Record<PumpStatus, string> = {
  ACTIVE: 'bg-green-100 text-green-700',
  INACTIVE: 'bg-slate-100 text-slate-400',
  MAINTENANCE: 'bg-yellow-100 text-yellow-700',
}

export function BombasPage() {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editPump, setEditPump] = useState<Pump | undefined>()

  const { data: pumps = [], isLoading } = useQuery({
    queryKey: ['pumps', station?.id],
    queryFn: () => getPumps(station!.id),
    enabled: !!station,
  })

  const statusMutation = useMutation({
    mutationFn: ({ id, status }: { id: string; status: PumpStatus }) =>
      patchPumpStatus(id, status),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['pumps', station?.id] })
    },
    onError: () => toast.error('Erro ao alterar status'),
  })

  function openCreate() {
    setEditPump(undefined)
    setSheetOpen(true)
  }

  function openEdit(pump: Pump) {
    setEditPump(pump)
    setSheetOpen(true)
  }

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center p-6 text-sm text-slate-400">
        Selecione um posto para gerenciar bombas.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title="Bombas"
        actions={
          <Button size="sm" onClick={openCreate} className="bg-orange-600 hover:bg-orange-700">
            <Plus size={14} className="mr-1" /> Nova bomba
          </Button>
        }
      />

      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : pumps.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhuma bomba cadastrada neste posto.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Número</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Status</th>
                  <th className="px-4 py-2" />
                </tr>
              </thead>
              <tbody>
                {pumps.map((pump) => (
                  <tr key={pump.id} className="border-b last:border-0">
                    <td className="px-4 py-3 font-semibold text-slate-800">Bomba {pump.number}</td>
                    <td className="px-4 py-3">
                      <Select
                        value={pump.status}
                        onValueChange={(value) =>
                          statusMutation.mutate({ id: pump.id, status: value as PumpStatus })
                        }
                      >
                        <SelectTrigger className="w-36 h-7 text-xs">
                          <SelectValue>
                            <Badge className={STATUS_COLORS[pump.status]}>
                              {STATUS_LABELS[pump.status]}
                            </Badge>
                          </SelectValue>
                        </SelectTrigger>
                        <SelectContent>
                          {(Object.keys(STATUS_LABELS) as PumpStatus[]).map((s) => (
                            <SelectItem key={s} value={s} className="text-xs">
                              {STATUS_LABELS[s]}
                            </SelectItem>
                          ))}
                        </SelectContent>
                      </Select>
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(pump)}
                        className="text-slate-400 hover:text-slate-600"
                        title="Editar"
                      >
                        <Pencil size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <PumpSheet open={sheetOpen} onOpenChange={setSheetOpen} pump={editPump} />
    </div>
  )
}
```

- [ ] **Step 3: Criar `frontend/src/components/cadastros/NozzleSheet.tsx`**

```typescript
import { useEffect } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { createNozzle, updateNozzle } from '@/api/nozzles'
import { getPumps } from '@/api/pumps'
import { getFuels } from '@/api/fuels'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { Nozzle } from '@/types'

const schema = z.object({
  number: z.coerce.number({ invalid_type_error: 'Obrigatório' }).int().min(1),
  pumpId: z.string().min(1, 'Selecione uma bomba'),
  fuelId: z.string().min(1, 'Selecione um combustível'),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  nozzle?: Nozzle & { pumpNumber?: number; fuelName?: string }
}

export function NozzleSheet({ open, onOpenChange, nozzle }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const isEdit = !!nozzle

  const { data: pumps = [] } = useQuery({
    queryKey: ['pumps', station?.id],
    queryFn: () => getPumps(station!.id),
    enabled: !!station && open,
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
    enabled: open,
  })

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (open) {
      reset({
        number: nozzle?.number ?? undefined,
        pumpId: nozzle?.pumpId ?? '',
        fuelId: nozzle?.fuelId ?? '',
      })
    }
  }, [open, nozzle, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? updateNozzle(nozzle.id, data)
        : createNozzle(data.pumpId, { number: data.number, pumpId: data.pumpId, fuelId: data.fuelId }),
    onSuccess: () => {
      toast.success(isEdit ? 'Bico atualizado!' : 'Bico criado!')
      pumps.forEach((p) => qc.invalidateQueries({ queryKey: ['nozzles', p.id] }))
      onOpenChange(false)
    },
    onError: () => {
      toast.error(isEdit ? 'Erro ao atualizar bico' : 'Erro ao criar bico')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[360px]">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar bico' : 'Novo bico'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Número do bico</Label>
            <Input type="number" min="1" placeholder="1" {...register('number')} />
            {errors.number && (
              <p className="mt-1 text-xs text-red-500">{errors.number.message}</p>
            )}
          </div>

          <div>
            <Label>Bomba</Label>
            <Controller
              control={control}
              name="pumpId"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Selecionar bomba…" />
                  </SelectTrigger>
                  <SelectContent>
                    {pumps.filter((p) => p.status === 'ACTIVE').map((pump) => (
                      <SelectItem key={pump.id} value={pump.id}>
                        Bomba {pump.number}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.pumpId && (
              <p className="mt-1 text-xs text-red-500">{errors.pumpId.message}</p>
            )}
          </div>

          <div>
            <Label>Combustível</Label>
            <Controller
              control={control}
              name="fuelId"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Selecionar combustível…" />
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

          <Button
            type="submit"
            disabled={mutation.isPending || !station}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar' : 'Criar bico'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
```

- [ ] **Step 4: Atualizar `frontend/src/pages/BicosPage.tsx`**

```typescript
import { useState } from 'react'
import { useQuery, useQueries } from '@tanstack/react-query'
import { Pencil, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { TopBar } from '@/components/layout/TopBar'
import { NozzleSheet } from '@/components/cadastros/NozzleSheet'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getPumps } from '@/api/pumps'
import { getNozzles, patchNozzleStatus } from '@/api/nozzles'
import { getFuels } from '@/api/fuels'
import type { Nozzle } from '@/types'

export function BicosPage() {
  const { station } = useActiveStation()
  const [filterPumpId, setFilterPumpId] = useState<string>('all')
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editNozzle, setEditNozzle] = useState<Nozzle | undefined>()

  const { data: pumps = [], isLoading: pumpsLoading } = useQuery({
    queryKey: ['pumps', station?.id],
    queryFn: () => getPumps(station!.id),
    enabled: !!station,
  })

  const nozzleResults = useQueries({
    queries: pumps.map((pump) => ({
      queryKey: ['nozzles', pump.id],
      queryFn: () => getNozzles(pump.id),
      enabled: pumps.length > 0,
    })),
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
  })

  const fuelById = Object.fromEntries(fuels.map((f) => [f.id, f]))
  const pumpById = Object.fromEntries(pumps.map((p) => [p.id, p]))

  const allNozzles = nozzleResults.flatMap((r) => r.data ?? [])
  const filteredNozzles =
    filterPumpId === 'all' ? allNozzles : allNozzles.filter((n) => n.pumpId === filterPumpId)

  function openCreate() {
    setEditNozzle(undefined)
    setSheetOpen(true)
  }

  function openEdit(nozzle: Nozzle) {
    setEditNozzle(nozzle)
    setSheetOpen(true)
  }

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center p-6 text-sm text-slate-400">
        Selecione um posto para gerenciar bicos.
      </div>
    )
  }

  const queryKeysForNozzle = (pumpId: string) => [['nozzles', pumpId]]

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title="Bicos"
        actions={
          <div className="flex items-center gap-2">
            <Select value={filterPumpId} onValueChange={setFilterPumpId}>
              <SelectTrigger className="h-8 w-36 text-xs">
                <SelectValue placeholder="Filtrar bomba" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">Todas as bombas</SelectItem>
                {pumps.map((p) => (
                  <SelectItem key={p.id} value={p.id}>
                    Bomba {p.number}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            <Button size="sm" onClick={openCreate} className="bg-orange-600 hover:bg-orange-700">
              <Plus size={14} className="mr-1" /> Novo bico
            </Button>
          </div>
        }
      />

      <div className="flex-1 overflow-auto p-6">
        {pumpsLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : filteredNozzles.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum bico cadastrado neste posto.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Bico</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Bomba</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Combustível</th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400">Status</th>
                  <th className="px-4 py-2" />
                </tr>
              </thead>
              <tbody>
                {filteredNozzles.map((nozzle) => (
                  <tr key={nozzle.id} className="border-b last:border-0">
                    <td className="px-4 py-3 font-semibold text-slate-800">Bico {nozzle.number}</td>
                    <td className="px-4 py-3 text-slate-500">
                      Bomba {pumpById[nozzle.pumpId]?.number ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-slate-500">
                      {fuelById[nozzle.fuelId]?.name ?? '—'}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <StatusToggle
                        id={nozzle.id}
                        active={nozzle.active}
                        queryKeys={queryKeysForNozzle(nozzle.pumpId)}
                        onToggle={(id, active) => patchNozzleStatus(id, active)}
                      />
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(nozzle)}
                        className="text-slate-400 hover:text-slate-600"
                        title="Editar"
                      >
                        <Pencil size={14} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <NozzleSheet open={sheetOpen} onOpenChange={setSheetOpen} nozzle={editNozzle} />
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

**Bombas:**
1. `/cadastros/bombas` — lista as bombas do posto ativo
2. Dropdown de status → muda de ACTIVE/INACTIVE/MAINTENANCE inline
3. "+ Nova bomba" → sheet com campo de número
4. ✏️ → edita número

**Bicos:**
1. `/cadastros/bicos` — lista bicos com bomba e combustível
2. Filtro de bomba filtra a lista
3. Toggle de status inline (ativo/inativo)
4. "+ Novo bico" → sheet com número + select de bomba ativa + select de combustível ativo
5. ✏️ → edita com dados pré-preenchidos

- [ ] **Step 7: Commit**

```bash
cd ..
git add frontend/src/components/cadastros/PumpSheet.tsx frontend/src/components/cadastros/NozzleSheet.tsx frontend/src/pages/BombasPage.tsx frontend/src/pages/BicosPage.tsx
git commit -m "feat(frontend): cadastro de bombas e bicos"
```
