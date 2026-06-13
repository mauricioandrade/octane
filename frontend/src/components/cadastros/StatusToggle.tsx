import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'

type Props = {
  id: string
  active: boolean
  queryKeys: string[][]
  onToggle: (id: string, active: boolean) => Promise<unknown>
  labelActive?: string
  labelInactive?: string
}

export function StatusToggle({
  id,
  active,
  queryKeys,
  onToggle,
  labelActive = 'Ativo',
  labelInactive = 'Inativo',
}: Props) {
  const qc = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => onToggle(id, !active),
    onSuccess: () => {
      queryKeys.forEach((key) => qc.invalidateQueries({ queryKey: key }))
    },
    onError: () => {
      toast.error('Erro ao alterar status')
    },
  })

  return (
    <button
      onClick={() => mutation.mutate()}
      disabled={mutation.isPending}
      title="Clique para alternar status"
      className="disabled:opacity-50"
    >
      <Badge
        className={cn(
          'cursor-pointer select-none',
          active
            ? 'bg-green-100 text-green-700 hover:bg-green-200'
            : 'bg-slate-100 text-slate-400 hover:bg-slate-200',
        )}
      >
        {active ? labelActive : labelInactive}
      </Badge>
    </button>
  )
}
