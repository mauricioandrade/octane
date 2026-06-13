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
