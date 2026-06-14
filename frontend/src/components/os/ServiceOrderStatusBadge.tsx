import { cn } from '@/lib/utils'
import type { ServiceOrderStatus } from '@/types'

const STATUS_LABEL: Record<ServiceOrderStatus, string> = {
  OPEN: 'Aberta',
  CLOSED: 'Fechada',
  CANCELLED: 'Cancelada',
}

const STATUS_CLASS: Record<ServiceOrderStatus, string> = {
  OPEN: 'bg-blue-100 text-blue-700 dark:bg-blue-900/40 dark:text-blue-300',
  CLOSED: 'bg-green-100 text-green-700 dark:bg-green-900/40 dark:text-green-300',
  CANCELLED: 'bg-red-100 text-red-600 dark:bg-red-900/40 dark:text-red-400',
}

type Props = {
  status: ServiceOrderStatus
  className?: string
}

export function ServiceOrderStatusBadge({ status, className }: Props) {
  return (
    <span
      className={cn(
        'inline-flex items-center rounded-full px-2 py-0.5 text-xs font-semibold',
        STATUS_CLASS[status],
        className,
      )}
    >
      {STATUS_LABEL[status]}
    </span>
  )
}
