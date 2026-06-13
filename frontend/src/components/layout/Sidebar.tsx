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
