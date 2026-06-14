type Props = {
  spend: number
  limit?: number
}

export function SpendProgress({ spend, limit }: Props) {
  if (!limit) return <span className="text-sm text-slate-500">R$ {spend.toFixed(2)}</span>

  const pct = Math.min((spend / limit) * 100, 100)
  const color = pct >= 90 ? 'bg-red-500' : pct >= 70 ? 'bg-yellow-500' : 'bg-green-500'

  return (
    <div className="flex flex-col gap-1">
      <span className="text-xs text-slate-500">
        R$ {spend.toFixed(2)} / R$ {limit.toFixed(2)}
      </span>
      <div className="h-1.5 w-full rounded-full bg-slate-200 dark:bg-slate-700">
        <div className={`h-1.5 rounded-full ${color}`} style={{ width: `${pct}%` }} />
      </div>
    </div>
  )
}
