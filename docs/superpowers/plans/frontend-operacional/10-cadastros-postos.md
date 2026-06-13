# Task 10 — Cadastros: StatusToggle + StationSheet + PostosPage + Sidebar station picker

**Files:**
- Create: `frontend/src/components/cadastros/StatusToggle.tsx`
- Create: `frontend/src/components/cadastros/StationSheet.tsx`
- Modify: `frontend/src/pages/PostosPage.tsx`
- Modify: `frontend/src/components/layout/Sidebar.tsx` (station picker funcional)

---

- [ ] **Step 1: Criar `frontend/src/components/cadastros/StatusToggle.tsx`**

Toggle genérico ativo/inativo para uso inline em tabelas:

```typescript
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

type Props = {
  id: string
  active: boolean
  queryKeys: string[][]
  onToggle: (id: string, active: boolean) => Promise<unknown>
  labelActive?: string
  labelInactive?: string
}

export function StatusToggle({
  id,
  active,
  queryKeys,
  onToggle,
  labelActive = 'Ativo',
  labelInactive = 'Inativo',
}: Props) {
  const qc = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => onToggle(id, !active),
    onSuccess: () => {
      queryKeys.forEach((key) => qc.invalidateQueries({ queryKey: key }))
    },
    onError: () => {
      toast.error('Erro ao alterar status')
    },
  })

  return (
    <button
      onClick={() => mutation.mutate()}
      disabled={mutation.isPending}
      title="Clique para alternar status"
      className="disabled:opacity-50"
    >
      <Badge
        className={cn(
          'cursor-pointer select-none',
          active
            ? 'bg-green-100 text-green-700 hover:bg-green-200'
            : 'bg-slate-100 text-slate-400 hover:bg-slate-200',
        )}
      >
        {active ? labelActive : labelInactive}
      </Badge>
    </button>
  )
}
```

- [ ] **Step 2: Criar `frontend/src/components/cadastros/StationSheet.tsx`**

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
import { createStation, updateStation } from '@/api/stations'
import type { Station } from '@/types'

const schema = z.object({
  name: z.string().min(1, 'Obrigatório').max(100),
  cnpj: z.string().min(1, 'Obrigatório').max(20),
  address: z.string().min(1, 'Obrigatório').max(200),
  city: z.string().min(1, 'Obrigatório').max(100),
  state: z.string().length(2, 'UF deve ter 2 caracteres'),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  station?: Station
}

export function StationSheet({ open, onOpenChange, station }: Props) {
  const qc = useQueryClient()
  const isEdit = !!station

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (open) {
      reset(
        station
          ? { name: station.name, cnpj: station.cnpj, address: station.address, city: station.city, state: station.state }
          : { name: '', cnpj: '', address: '', city: '', state: '' },
      )
    }
  }, [open, station, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit ? updateStation(station.id, data) : createStation(data),
    onSuccess: () => {
      toast.success(isEdit ? 'Posto atualizado!' : 'Posto criado!')
      qc.invalidateQueries({ queryKey: ['stations'] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error(isEdit ? 'Erro ao atualizar posto' : 'Erro ao criar posto')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar posto' : 'Novo posto'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Nome</Label>
            <Input placeholder="Ex: Posto Central" {...register('name')} />
            {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name.message}</p>}
          </div>

          <div>
            <Label>CNPJ</Label>
            <Input placeholder="00.000.000/0000-00" {...register('cnpj')} />
            {errors.cnpj && <p className="mt-1 text-xs text-red-500">{errors.cnpj.message}</p>}
          </div>

          <div>
            <Label>Endereço</Label>
            <Input placeholder="Rua, número" {...register('address')} />
            {errors.address && <p className="mt-1 text-xs text-red-500">{errors.address.message}</p>}
          </div>

          <div className="grid grid-cols-[1fr_60px] gap-3">
            <div>
              <Label>Cidade</Label>
              <Input placeholder="São Paulo" {...register('city')} />
              {errors.city && <p className="mt-1 text-xs text-red-500">{errors.city.message}</p>}
            </div>
            <div>
              <Label>UF</Label>
              <Input
                placeholder="SP"
                maxLength={2}
                className="uppercase"
                {...register('state')}
              />
              {errors.state && <p className="mt-1 text-xs text-red-500">{errors.state.message}</p>}
            </div>
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar alterações' : 'Criar posto'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
```

- [ ] **Step 3: Atualizar `frontend/src/pages/PostosPage.tsx`**

```typescript
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Pencil, Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { StationSheet } from '@/components/cadastros/StationSheet'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { getStations, patchStationStatus } from '@/api/stations'
import type { Station } from '@/types'

export function PostosPage() {
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editStation, setEditStation] = useState<Station | undefined>()

  const { data: stations = [], isLoading } = useQuery({
    queryKey: ['stations'],
    queryFn: () => getStations(),
  })

  function openCreate() {
    setEditStation(undefined)
    setSheetOpen(true)
  }

  function openEdit(station: Station) {
    setEditStation(station)
    setSheetOpen(true)
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title="Postos"
        actions={
          <Button size="sm" onClick={openCreate} className="bg-orange-600 hover:bg-orange-700">
            <Plus size={14} className="mr-1" /> Novo posto
          </Button>
        }
      />

      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : stations.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum posto cadastrado.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Nome</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">CNPJ</th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">Cidade</th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400">Status</th>
                  <th className="px-4 py-2" />
                </tr>
              </thead>
              <tbody>
                {stations.map((s) => (
                  <tr key={s.id} className="border-b last:border-0">
                    <td className="px-4 py-3 font-semibold text-slate-800">{s.name}</td>
                    <td className="px-4 py-3 text-slate-500">{s.cnpj}</td>
                    <td className="px-4 py-3 text-slate-500">{s.city} — {s.state}</td>
                    <td className="px-4 py-3 text-center">
                      <StatusToggle
                        id={s.id}
                        active={s.active}
                        queryKeys={[['stations']]}
                        onToggle={(id, active) => patchStationStatus(id, active)}
                      />
                    </td>
                    <td className="px-4 py-3 text-right">
                      <button
                        onClick={() => openEdit(s)}
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

      <StationSheet
        open={sheetOpen}
        onOpenChange={setSheetOpen}
        station={editStation}
      />
    </div>
  )
}
```

- [ ] **Step 4: Atualizar station picker na Sidebar**

Substituir o handler simplificado da Sidebar por um dropdown que lista postos ativos:

```typescript
// Adicionar ao imports da Sidebar:
import { useQuery } from '@tanstack/react-query'
import { getStations } from '@/api/stations'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
```

Antes de executar, rodar:
```bash
cd frontend
npx shadcn@latest add dropdown-menu
```

Então substituir o bloco do station selector no rodapé da Sidebar:

```typescript
// Dentro de Sidebar(), adicionar:
const { data: activeStations = [] } = useQuery({
  queryKey: ['stations', 'active'],
  queryFn: () => getStations(true),
})

// Substituir o bloco <button onClick=...> do station selector por:
<DropdownMenu>
  <DropdownMenuTrigger asChild>
    <button className="flex w-full items-center justify-between rounded-md border bg-slate-50 px-3 py-2 text-left hover:bg-slate-100">
      <div>
        <p className="text-[9px] uppercase tracking-wider text-slate-400">Posto ativo</p>
        <p className="max-w-[130px] truncate text-xs font-semibold text-slate-800">
          {station?.name ?? 'Selecionar posto'}
        </p>
      </div>
      <ChevronsUpDown size={12} className="text-slate-400" />
    </button>
  </DropdownMenuTrigger>
  <DropdownMenuContent align="start" className="w-48">
    {activeStations.map((s) => (
      <DropdownMenuItem
        key={s.id}
        onClick={() => setStation({ id: s.id, name: s.name })}
        className={station?.id === s.id ? 'font-semibold text-orange-600' : ''}
      >
        {s.name}
      </DropdownMenuItem>
    ))}
    {activeStations.length === 0 && (
      <DropdownMenuItem disabled>Nenhum posto ativo</DropdownMenuItem>
    )}
  </DropdownMenuContent>
</DropdownMenu>
```

O arquivo completo de `Sidebar.tsx` após a edição:

```typescript
import { NavLink } from 'react-router-dom'
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Fuel, DollarSign, Store, ClipboardList, ChevronDown, Moon, Sun, ChevronsUpDown } from 'lucide-react'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '@/components/ui/dropdown-menu'
import { cn } from '@/lib/utils'
import { getTheme, toggleTheme, type Theme } from '@/lib/theme'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getStations } from '@/api/stations'

const cadastrosItems = [
  { to: '/cadastros/postos', label: 'Postos' },
  { to: '/cadastros/bombas', label: 'Bombas' },
  { to: '/cadastros/bicos', label: 'Bicos' },
  { to: '/cadastros/combustiveis', label: 'Combustíveis' },
]

export function Sidebar() {
  const { station, setStation } = useActiveStation()
  const [cadastrosOpen, setCadastrosOpen] = useState(false)
  const [theme, setThemeState] = useState<Theme>(getTheme)

  const { data: activeStations = [] } = useQuery({
    queryKey: ['stations', 'active'],
    queryFn: () => getStations(true),
  })

  function handleThemeToggle() {
    const next = toggleTheme()
    setThemeState(next)
  }

  return (
    <aside className="flex w-52 shrink-0 flex-col border-r bg-white">
      <div className="border-b px-4 py-4">
        <span className="text-[17px] font-extrabold tracking-tight text-orange-600">
          ⛽ Octane
        </span>
      </div>

      <nav className="flex flex-1 flex-col gap-0.5 p-2">
        <NavLink
          to="/pista"
          className={({ isActive }) =>
            cn('flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive ? 'bg-orange-50 font-semibold text-orange-600' : 'text-slate-500 hover:bg-slate-50')
          }
        >
          <Fuel size={15} /> Pista
        </NavLink>

        <NavLink
          to="/precos"
          className={({ isActive }) =>
            cn('flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive ? 'bg-orange-50 font-semibold text-orange-600' : 'text-slate-500 hover:bg-slate-50')
          }
        >
          <DollarSign size={15} /> Preços
        </NavLink>

        <button
          onClick={() => setCadastrosOpen((v) => !v)}
          className="flex items-center gap-2 rounded-md px-3 py-2 text-sm text-slate-500 hover:bg-slate-50"
        >
          <Store size={15} /> Cadastros
          <ChevronDown size={12} className={cn('ml-auto transition-transform', cadastrosOpen && 'rotate-180')} />
        </button>
        {cadastrosOpen && (
          <div className="ml-6 flex flex-col gap-0.5">
            {cadastrosItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  cn('rounded px-3 py-1.5 text-xs',
                    isActive ? 'font-semibold text-orange-600' : 'text-slate-400 hover:text-slate-600')
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>
        )}

        <NavLink
          to="/historico"
          className={({ isActive }) =>
            cn('flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive ? 'bg-orange-50 font-semibold text-orange-600' : 'text-slate-500 hover:bg-slate-50')
          }
        >
          <ClipboardList size={15} /> Histórico
        </NavLink>
      </nav>

      <div className="border-t p-2 flex flex-col gap-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button className="flex w-full items-center justify-between rounded-md border bg-slate-50 px-3 py-2 text-left hover:bg-slate-100">
              <div>
                <p className="text-[9px] uppercase tracking-wider text-slate-400">Posto ativo</p>
                <p className="max-w-[130px] truncate text-xs font-semibold text-slate-800">
                  {station?.name ?? 'Selecionar posto'}
                </p>
              </div>
              <ChevronsUpDown size={12} className="text-slate-400" />
            </button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="start" className="w-48">
            {activeStations.map((s) => (
              <DropdownMenuItem
                key={s.id}
                onClick={() => setStation({ id: s.id, name: s.name })}
                className={station?.id === s.id ? 'font-semibold text-orange-600' : ''}
              >
                {s.name}
              </DropdownMenuItem>
            ))}
            {activeStations.length === 0 && (
              <DropdownMenuItem disabled>Nenhum posto ativo</DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>

        <div className="flex items-center justify-between px-1">
          <span className="text-[10px] text-slate-400">Tema escuro</span>
          <button onClick={handleThemeToggle} aria-label="Toggle theme">
            {theme === 'dark' ? (
              <Sun size={14} className="text-slate-400" />
            ) : (
              <Moon size={14} className="text-slate-400" />
            )}
          </button>
        </div>
      </div>
    </aside>
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

1. Navegar para `/cadastros/postos`
2. Lista de postos com CNPJ, cidade, status
3. Badge de status clicável → alterna ativo/inativo
4. Botão ✏️ → abre sheet com dados pré-preenchidos
5. Botão "+ Novo posto" → abre sheet vazia
6. Salvar → toast + lista atualiza
7. Sidebar → dropdown de posto ativo lista postos ativos
8. Selecionar posto → persiste em localStorage + nome aparece na sidebar

- [ ] **Step 7: Commit**

```bash
cd ..
git add frontend/src/components/cadastros/StatusToggle.tsx frontend/src/components/cadastros/StationSheet.tsx frontend/src/pages/PostosPage.tsx frontend/src/components/layout/Sidebar.tsx
git commit -m "feat(frontend): cadastro de postos e seletor de posto na sidebar"
```
