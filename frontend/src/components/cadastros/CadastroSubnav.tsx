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
    <nav className="w-40 shrink-0 border-r bg-white dark:bg-slate-900 flex flex-col gap-0.5 p-2">
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
                : 'text-slate-500 dark:text-slate-400 hover:bg-slate-50 dark:hover:bg-slate-800',
            )
          }
        >
          {item.label}
        </NavLink>
      ))}
    </nav>
  )
}
