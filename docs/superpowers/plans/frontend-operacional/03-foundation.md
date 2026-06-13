# Task 03 — Foundation: StationContext, lib/theme, hooks

**Files:**
- Modify: `frontend/src/lib/utils.ts` (adicionar formatBRL e formatLiters)
- Create: `frontend/src/lib/theme.ts`
- Create: `frontend/src/context/StationContext.tsx`
- Create: `frontend/src/hooks/useActiveStation.ts`
- Modify: `frontend/src/main.tsx`

---

- [ ] **Step 1: Atualizar `frontend/src/lib/utils.ts`**

O shadcn já criou esse arquivo com `cn()`. Adicionar as duas funções de formatação:

```typescript
import { clsx, type ClassValue } from 'clsx'
import { twMerge } from 'tailwind-merge'

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export function formatBRL(value: number): string {
  return new Intl.NumberFormat('pt-BR', {
    style: 'currency',
    currency: 'BRL',
  }).format(value)
}

export function formatLiters(value: number): string {
  return (
    new Intl.NumberFormat('pt-BR', {
      minimumFractionDigits: 3,
      maximumFractionDigits: 3,
    }).format(value) + ' L'
  )
}
```

- [ ] **Step 2: Criar `frontend/src/lib/theme.ts`**

```typescript
export type Theme = 'light' | 'dark'

export function getTheme(): Theme {
  return (localStorage.getItem('octane-theme') as Theme) ?? 'light'
}

export function setTheme(theme: Theme): void {
  localStorage.setItem('octane-theme', theme)
  document.documentElement.classList.toggle('dark', theme === 'dark')
}

export function initTheme(): void {
  setTheme(getTheme())
}

export function toggleTheme(): Theme {
  const next = getTheme() === 'light' ? 'dark' : 'light'
  setTheme(next)
  return next
}
```

- [ ] **Step 3: Criar `frontend/src/context/StationContext.tsx`**

```typescript
import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'

export type ActiveStation = {
  id: string
  name: string
}

type StationContextValue = {
  station: ActiveStation | null
  setStation: (s: ActiveStation) => void
}

const StationContext = createContext<StationContextValue | null>(null)

const STORAGE_KEY = 'octane-station'

export function StationProvider({ children }: { children: ReactNode }) {
  const [station, setStationState] = useState<ActiveStation | null>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      return saved ? (JSON.parse(saved) as ActiveStation) : null
    } catch {
      return null
    }
  })

  function setStation(s: ActiveStation) {
    setStationState(s)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(s))
  }

  // Detecta se o posto salvo ainda existe — se não, limpa
  // (tratado via error boundary em cada page que usa station)

  return (
    <StationContext.Provider value={{ station, setStation }}>
      {children}
    </StationContext.Provider>
  )
}

export function useStationContext(): StationContextValue {
  const ctx = useContext(StationContext)
  if (!ctx) throw new Error('useStationContext must be used within StationProvider')
  return ctx
}
```

- [ ] **Step 4: Criar `frontend/src/hooks/useActiveStation.ts`**

```typescript
import { useStationContext } from '@/context/StationContext'

export function useActiveStation() {
  return useStationContext()
}
```

- [ ] **Step 5: Atualizar `frontend/src/main.tsx` para inicializar tema**

```typescript
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryProvider } from './QueryProvider'
import { App } from './App'
import { initTheme } from './lib/theme'
import './index.css'

initTheme()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryProvider>
      <App />
    </QueryProvider>
  </StrictMode>,
)
```

- [ ] **Step 6: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros.

- [ ] **Step 7: Commit**

```bash
cd ..
git add frontend/src/lib/utils.ts frontend/src/lib/theme.ts frontend/src/context/StationContext.tsx frontend/src/hooks/useActiveStation.ts frontend/src/main.tsx
git commit -m "feat(frontend): StationContext, theme toggle e utilitários"
```
