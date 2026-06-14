import { Menu } from 'lucide-react'
import { useActiveStation } from '@/hooks/useActiveStation'
import { useSidebar } from '@/context/SidebarContext'

type TopBarProps = {
  title: string
  subtitle?: string
  actions?: React.ReactNode
}

export function TopBar({ title, subtitle, actions }: TopBarProps) {
  const { station } = useActiveStation()
  const { toggle } = useSidebar()

  return (
    <div className="flex items-center justify-between border-b bg-white dark:bg-slate-900 px-4 py-3 md:px-6">
      <div className="flex items-center gap-3">
        <button
          onClick={toggle}
          className="rounded p-1 text-slate-400 hover:text-slate-600 md:hidden dark:text-slate-500"
          aria-label="Menu"
        >
          <Menu size={20} />
        </button>
        <div>
          <h1 className="text-lg font-bold text-slate-900 dark:text-slate-100">{title}</h1>
          {(subtitle ?? station?.name) && (
            <p className="text-xs text-slate-400 dark:text-slate-500">
              {subtitle ?? station?.name}
            </p>
          )}
        </div>
      </div>
      {actions && <div className="flex items-center gap-2">{actions}</div>}
    </div>
  )
}
