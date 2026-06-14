import { AlertTriangle } from 'lucide-react'

type Props = {
  previous?: number
  current: number
}

export function OdometerAlert({ previous, current }: Props) {
  return (
    <span className="inline-flex items-center gap-1 rounded bg-yellow-100 px-2 py-0.5 text-xs font-medium text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-400">
      <AlertTriangle size={11} />
      {current} km{previous !== undefined ? ` (ant. ${previous} km)` : ''}
    </span>
  )
}
