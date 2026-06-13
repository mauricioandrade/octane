# Task 04 — Shell: AppShell, Sidebar, TopBar, App.tsx + page stubs

**Files:**
- Create: `frontend/src/components/layout/AppShell.tsx`
- Create: `frontend/src/components/layout/Sidebar.tsx`
- Create: `frontend/src/components/layout/TopBar.tsx`
- Create: `frontend/src/pages/PistaPage.tsx` (stub)
- Create: `frontend/src/pages/PrecosPage.tsx` (stub)
- Create: `frontend/src/pages/CadastrosPage.tsx` (stub)
- Create: `frontend/src/pages/PostosPage.tsx` (stub)
- Create: `frontend/src/pages/BombasPage.tsx` (stub)
- Create: `frontend/src/pages/BicosPage.tsx` (stub)
- Create: `frontend/src/pages/CombustiveisPage.tsx` (stub)
- Create: `frontend/src/pages/HistoricoPage.tsx` (stub)
- Modify: `frontend/src/App.tsx`

---

- [ ] **Step 1: Criar page stubs**

Crie 8 arquivos, todos com o mesmo padrão (substituindo o nome):

`frontend/src/pages/PistaPage.tsx`:
```typescript
export function PistaPage() {
  return <div className="p-6">Pista</div>
}
```

`frontend/src/pages/PrecosPage.tsx`:
```typescript
export function PrecosPage() {
  return <div className="p-6">Preços</div>
}
```

`frontend/src/pages/CadastrosPage.tsx`:
```typescript
import { Outlet } from 'react-router-dom'
import { CadastroSubnav } from '@/components/cadastros/CadastroSubnav'

export function CadastrosPage() {
  return (
    <div className="flex h-full">
      <CadastroSubnav />
      <div className="flex-1 overflow-auto">
        <Outlet />
      </div>
    </div>
  )
}
```

`frontend/src/pages/PostosPage.tsx`:
```typescript
export function PostosPage() {
  return <div className="p-6">Postos</div>
}
```

`frontend/src/pages/BombasPage.tsx`:
```typescript
export function BombasPage() {
  return <div className="p-6">Bombas</div>
}
```

`frontend/src/pages/BicosPage.tsx`:
```typescript
export function BicosPage() {
  return <div className="p-6">Bicos</div>
}
```

`frontend/src/pages/CombustiveisPage.tsx`:
```typescript
export function CombustiveisPage() {
  return <div className="p-6">Combustíveis</div>
}
```

`frontend/src/pages/HistoricoPage.tsx`:
```typescript
export function HistoricoPage() {
  return <div className="p-6">Histórico</div>
}
```

- [ ] **Step 2: Criar `frontend/src/components/cadastros/CadastroSubnav.tsx` (stub)**

Necessário para CadastrosPage compilar:

```typescript
import { NavLink } from 'react-router-dom'
import { cn } from '@/lib/utils'

const items = [
  { to: '/cadastros/postos', label: 'Postos' },
  { to: '/cadastros/bombas', label: 'Bombas' },
  { to: '/cadastros/bicos', label: 'Bicos' },
  { to: '/cadastros/combustiveis', label: 'Combustíveis' },
]

export function CadastroSubnav() {
  return (
    <nav className="w-40 shrink-0 border-r bg-white flex flex-col gap-0.5 p-2">
      <p className="px-2 py-1 text-[10px] font-bold uppercase text-orange-600 tracking-wider">
        Cadastros
      </p>
      {items.map((item) => (
        <NavLink
          key={item.to}
          to={item.to}
          className={({ isActive }) =>
            cn(
              'rounded px-3 py-2 text-sm',
              isActive
                ? 'bg-orange-50 font-semibold text-orange-600'
                : 'text-slate-500 hover:bg-slate-50',
            )
          }
        >
          {item.label}
        </NavLink>
      ))}
    </nav>
  )
}
```

- [ ] **Step 3: Criar `frontend/src/components/layout/TopBar.tsx`**

```typescript
import { useActiveStation } from '@/hooks/useActiveStation'

type TopBarProps = {
  title: string
  subtitle?: string
  actions?: React.ReactNode
}

export function TopBar({ title, subtitle, actions }: TopBarProps) {
  const { station } = useActiveStation()

  return (
    <div className="flex items-center justify-between border-b bg-white px-6 py-3">
      <div>
        <h1 className="text-lg font-bold text-slate-900">{title}</h1>
        {(subtitle ?? station?.name) && (
          <p className="text-xs text-slate-400">
            {subtitle ?? station?.name}
          </p>
        )}
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  )
}
```

- [ ] **Step 4: Criar `frontend/src/components/layout/Sidebar.tsx`**

```typescript
import { NavLink, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { Fuel, DollarSign, Store, ClipboardList, ChevronDown, Moon, Sun, ChevronsUpDown } from 'lucide-react'
import { cn } from '@/lib/utils'
import { getTheme, toggleTheme, type Theme } from '@/lib/theme'
import { useActiveStation } from '@/hooks/useActiveStation'

const cadastrosItems = [
  { to: '/cadastros/postos', label: 'Postos' },
  { to: '/cadastros/bombas', label: 'Bombas' },
  { to: '/cadastros/bicos', label: 'Bicos' },
  { to: '/cadastros/combustiveis', label: 'Combustíveis' },
]

export function Sidebar() {
  const { station } = useActiveStation()
  const navigate = useNavigate()
  const [cadastrosOpen, setCadastrosOpen] = useState(false)
  const [theme, setTheme] = useState<Theme>(getTheme)

  function handleThemeToggle() {
    const next = toggleTheme()
    setTheme(next)
  }

  return (
    <aside className="flex w-52 shrink-0 flex-col border-r bg-white">
      {/* Logo */}
      <div className="border-b px-4 py-4">
        <span className="text-[17px] font-extrabold tracking-tight text-orange-600">
          ⛽ Octane
        </span>
      </div>

      {/* Nav */}
      <nav className="flex flex-1 flex-col gap-0.5 p-2">
        <NavLink
          to="/pista"
          className={({ isActive }) =>
            cn(
              'flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive
                ? 'bg-orange-50 font-semibold text-orange-600'
                : 'text-slate-500 hover:bg-slate-50',
            )
          }
        >
          <Fuel size={15} /> Pista
        </NavLink>

        <NavLink
          to="/precos"
          className={({ isActive }) =>
            cn(
              'flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive
                ? 'bg-orange-50 font-semibold text-orange-600'
                : 'text-slate-500 hover:bg-slate-50',
            )
          }
        >
          <DollarSign size={15} /> Preços
        </NavLink>

        {/* Cadastros (expandível) */}
        <button
          onClick={() => setCadastrosOpen((v) => !v)}
          className="flex items-center gap-2 rounded-md px-3 py-2 text-sm text-slate-500 hover:bg-slate-50"
        >
          <Store size={15} /> Cadastros
          <ChevronDown
            size={12}
            className={cn('ml-auto transition-transform', cadastrosOpen && 'rotate-180')}
          />
        </button>
        {cadastrosOpen && (
          <div className="ml-6 flex flex-col gap-0.5">
            {cadastrosItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) =>
                  cn(
                    'rounded px-3 py-1.5 text-xs',
                    isActive
                      ? 'font-semibold text-orange-600'
                      : 'text-slate-400 hover:text-slate-600',
                  )
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
            cn(
              'flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive
                ? 'bg-orange-50 font-semibold text-orange-600'
                : 'text-slate-500 hover:bg-slate-50',
            )
          }
        >
          <ClipboardList size={15} /> Histórico
        </NavLink>
      </nav>

      {/* Footer: station selector + theme toggle */}
      <div className="border-t p-2 flex flex-col gap-2">
        {/* Station selector — stub: expansão completa na Task 10 */}
        <button
          onClick={() => navigate('/cadastros/postos')}
          className="flex items-center justify-between rounded-md border bg-slate-50 px-3 py-2 text-left"
        >
          <div>
            <p className="text-[9px] uppercase tracking-wider text-slate-400">Posto ativo</p>
            <p className="text-xs font-semibold text-slate-800 truncate max-w-[130px]">
              {station?.name ?? 'Selecionar posto'}
            </p>
          </div>
          <ChevronsUpDown size={12} className="text-slate-400" />
        </button>

        {/* Theme toggle */}
        <div className="flex items-center justify-between px-1">
          <span className="text-[10px] text-slate-400">Tema escuro</span>
          <button
            onClick={handleThemeToggle}
            aria-label="Toggle theme"
            className="flex items-center"
          >
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

- [ ] **Step 5: Criar `frontend/src/components/layout/AppShell.tsx`**

```typescript
import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'

export function AppShell() {
  return (
    <div className="flex h-screen overflow-hidden bg-slate-100">
      <Sidebar />
      <main className="flex flex-1 flex-col overflow-hidden">
        <Outlet />
      </main>
    </div>
  )
}
```

- [ ] **Step 6: Atualizar `frontend/src/App.tsx` com router e rotas**

```typescript
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'sonner'
import { AppShell } from '@/components/layout/AppShell'
import { StationProvider } from '@/context/StationContext'
import { PistaPage } from '@/pages/PistaPage'
import { PrecosPage } from '@/pages/PrecosPage'
import { CadastrosPage } from '@/pages/CadastrosPage'
import { PostosPage } from '@/pages/PostosPage'
import { BombasPage } from '@/pages/BombasPage'
import { BicosPage } from '@/pages/BicosPage'
import { CombustiveisPage } from '@/pages/CombustiveisPage'
import { HistoricoPage } from '@/pages/HistoricoPage'

export function App() {
  return (
    <BrowserRouter>
      <StationProvider>
        <Routes>
          <Route element={<AppShell />}>
            <Route index element={<Navigate to="/pista" replace />} />
            <Route path="/pista" element={<PistaPage />} />
            <Route path="/precos" element={<PrecosPage />} />
            <Route path="/cadastros" element={<CadastrosPage />}>
              <Route index element={<Navigate to="/cadastros/postos" replace />} />
              <Route path="postos" element={<PostosPage />} />
              <Route path="bombas" element={<BombasPage />} />
              <Route path="bicos" element={<BicosPage />} />
              <Route path="combustiveis" element={<CombustiveisPage />} />
            </Route>
            <Route path="/historico" element={<HistoricoPage />} />
          </Route>
        </Routes>
        <Toaster richColors position="top-right" />
      </StationProvider>
    </BrowserRouter>
  )
}
```

- [ ] **Step 7: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros TypeScript.

- [ ] **Step 8: Verificar no browser**

```bash
npm run dev
```

Abrir `http://localhost:5173`. Esperado:
- Sidebar visível com logo ⛽ Octane
- Nav com links Pista / Preços / Cadastros / Histórico
- Clicar em Cadastros expande sub-itens
- Cada link navega para a stub page correspondente

- [ ] **Step 9: Commit**

```bash
cd ..
git add frontend/src/App.tsx frontend/src/main.tsx frontend/src/components/layout/ frontend/src/components/cadastros/CadastroSubnav.tsx frontend/src/pages/
git commit -m "feat(frontend): shell, sidebar, roteamento e page stubs"
```
