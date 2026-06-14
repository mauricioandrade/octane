import { NavLink } from 'react-router-dom'
import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Fuel, DollarSign, Store, ClipboardList, ChevronDown, Moon, Sun, ChevronsUpDown, X, LogOut, Truck, Wrench, BadgePercent } from 'lucide-react'
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
import { useSidebar } from '@/context/SidebarContext'
import { useAuth } from '@/context/AuthContext'

const cadastrosItems = [
  { to: '/cadastros/postos', label: 'Postos' },
  { to: '/cadastros/bombas', label: 'Bombas' },
  { to: '/cadastros/bicos', label: 'Bicos' },
  { to: '/cadastros/combustiveis', label: 'Combustíveis' },
]

const frotaItems = [
  { to: '/frota/clientes', label: 'Clientes' },
  { to: '/frota/veiculos', label: 'Veículos' },
  { to: '/frota/motoristas', label: 'Motoristas' },
  { to: '/frota/relatorio', label: 'Relatório' },
]

const servicosItems = [
  { to: '/os', label: 'Ordens de Serviço' },
  { to: '/os/historico', label: 'Histórico por Placa' },
]

const comissoesItems = [
  { to: '/comissao/regras', label: 'Regras' },
  { to: '/comissao/entradas', label: 'Entradas' },
]

export function Sidebar() {
  const { station, setStation } = useActiveStation()
  const [cadastrosOpen, setCadastrosOpen] = useState(false)
  const [frotaOpen, setFrotaOpen] = useState(false)
  const [servicosOpen, setServicosOpen] = useState(false)
  const [comissoesOpen, setComissoesOpen] = useState(false)
  const [theme, setThemeState] = useState<Theme>(getTheme)
  const { isOpen, close } = useSidebar()
  const { logout } = useAuth()

  const { data: activeStations = [] } = useQuery({
    queryKey: ['stations', 'active'],
    queryFn: () => getStations(true),
  })

  function handleThemeToggle() {
    const next = toggleTheme()
    setThemeState(next)
  }

  return (
    <aside className={cn(
      'flex w-52 shrink-0 flex-col border-r bg-white dark:bg-slate-900',
      'fixed inset-y-0 left-0 z-50 transition-transform duration-200',
      'md:static md:translate-x-0',
      isOpen ? 'translate-x-0' : '-translate-x-full'
    )}>
      <div className="border-b px-4 py-4 flex items-center">
        <span className="text-[17px] font-extrabold tracking-tight text-orange-600">
          ⛽ Octane
        </span>
        <button
          onClick={close}
          className="ml-auto rounded p-1 text-slate-400 hover:text-slate-600 md:hidden dark:text-slate-500"
          aria-label="Fechar menu"
        >
          <X size={16} />
        </button>
      </div>

      <nav className="flex flex-1 flex-col gap-0.5 p-2">
        <NavLink
          to="/pista"
          onClick={close}
          className={({ isActive }) =>
            cn('flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive ? 'bg-orange-50 font-semibold text-orange-600' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800')
          }
        >
          <Fuel size={15} /> Pista
        </NavLink>

        <NavLink
          to="/precos"
          onClick={close}
          className={({ isActive }) =>
            cn('flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive ? 'bg-orange-50 font-semibold text-orange-600' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800')
          }
        >
          <DollarSign size={15} /> Preços
        </NavLink>

        <button
          onClick={() => setCadastrosOpen((v) => !v)}
          className="flex items-center gap-2 rounded-md px-3 py-2 text-sm text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800"
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
                onClick={close}
                className={({ isActive }) =>
                  cn('rounded px-3 py-1.5 text-xs',
                    isActive ? 'font-semibold text-orange-600' : 'text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300')
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>
        )}

        <NavLink
          to="/historico"
          onClick={close}
          className={({ isActive }) =>
            cn('flex items-center gap-2 rounded-md px-3 py-2 text-sm',
              isActive ? 'bg-orange-50 font-semibold text-orange-600' : 'text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800')
          }
        >
          <ClipboardList size={15} /> Histórico
        </NavLink>

        <button
          onClick={() => setFrotaOpen((v) => !v)}
          className="flex items-center gap-2 rounded-md px-3 py-2 text-sm text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800"
        >
          <Truck size={15} /> Frota
          <ChevronDown size={12} className={cn('ml-auto transition-transform', frotaOpen && 'rotate-180')} />
        </button>
        {frotaOpen && (
          <div className="ml-6 flex flex-col gap-0.5">
            {frotaItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={close}
                className={({ isActive }) =>
                  cn('rounded px-3 py-1.5 text-xs',
                    isActive ? 'font-semibold text-orange-600' : 'text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300')
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>
        )}

        <button
          onClick={() => setServicosOpen((v) => !v)}
          className="flex items-center gap-2 rounded-md px-3 py-2 text-sm text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800"
        >
          <Wrench size={15} /> Serviços
          <ChevronDown size={12} className={cn('ml-auto transition-transform', servicosOpen && 'rotate-180')} />
        </button>
        {servicosOpen && (
          <div className="ml-6 flex flex-col gap-0.5">
            {servicosItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.to === '/os'}
                onClick={close}
                className={({ isActive }) =>
                  cn('rounded px-3 py-1.5 text-xs',
                    isActive ? 'font-semibold text-orange-600' : 'text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300')
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>
        )}

        <button
          onClick={() => setComissoesOpen((v) => !v)}
          className="flex items-center gap-2 rounded-md px-3 py-2 text-sm text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800"
        >
          <BadgePercent size={15} /> Comissões
          <ChevronDown size={12} className={cn('ml-auto transition-transform', comissoesOpen && 'rotate-180')} />
        </button>
        {comissoesOpen && (
          <div className="ml-6 flex flex-col gap-0.5">
            {comissoesItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                onClick={close}
                className={({ isActive }) =>
                  cn('rounded px-3 py-1.5 text-xs',
                    isActive ? 'font-semibold text-orange-600' : 'text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300')
                }
              >
                {item.label}
              </NavLink>
            ))}
          </div>
        )}
      </nav>

      <div className="border-t p-2 flex flex-col gap-2">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <button className="flex w-full items-center justify-between rounded-md border bg-slate-50 dark:bg-slate-800 dark:border-slate-700 px-3 py-2 text-left hover:bg-slate-100 dark:hover:bg-slate-700">
              <div>
                <p className="text-[9px] uppercase tracking-wider text-slate-400 dark:text-slate-500">Posto ativo</p>
                <p className="max-w-[130px] truncate text-xs font-semibold text-slate-800 dark:text-slate-100">
                  {station?.name ?? 'Selecionar posto'}
                </p>
              </div>
              <ChevronsUpDown size={12} className="text-slate-400 dark:text-slate-500" />
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
          <span className="text-[10px] text-slate-400 dark:text-slate-500">Tema escuro</span>
          <button onClick={handleThemeToggle} aria-label="Toggle theme">
            {theme === 'dark' ? (
              <Sun size={14} className="text-slate-400 dark:text-slate-500" />
            ) : (
              <Moon size={14} className="text-slate-400 dark:text-slate-500" />
            )}
          </button>
        </div>

        <button
          onClick={() => logout()}
          className="flex w-full items-center gap-2 rounded-md px-3 py-1.5 text-xs text-slate-400 dark:text-slate-500 hover:bg-slate-100 dark:hover:bg-slate-800 hover:text-slate-600 dark:hover:text-slate-300"
        >
          <LogOut size={13} />
          Sair
        </button>
      </div>
    </aside>
  )
}
