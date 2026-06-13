import { useQuery } from '@tanstack/react-query'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { Skeleton } from '@/components/ui/skeleton'
import { getPriceHistory } from '@/api/prices'
import { useActiveStation } from '@/hooks/useActiveStation'
import { formatBRL } from '@/lib/utils'

type Props = {
  fuelId: string
  fuelName: string
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function PriceHistoryModal({ fuelId, fuelName, open, onOpenChange }: Props) {
  const { station } = useActiveStation()

  const { data: history = [], isLoading } = useQuery({
    queryKey: ['price-history', station?.id, fuelId],
    queryFn: () => getPriceHistory(station!.id, fuelId),
    enabled: !!station && open,
  })

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-md">
        <DialogHeader>
          <DialogTitle>Histórico de preços — {fuelName}</DialogTitle>
        </DialogHeader>

        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
            <Skeleton className="h-8 w-full" />
          </div>
        ) : history.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum histórico disponível.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border">
            <table className="w-full text-sm">
              <thead className="bg-slate-50">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">
                    Preço (R$/L)
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">
                    Vigente desde
                  </th>
                </tr>
              </thead>
              <tbody>
                {history.map((entry, idx) => (
                  <tr key={entry.id} className="border-t">
                    <td className="px-4 py-2 font-semibold text-orange-600 tabular-nums">
                      {formatBRL(entry.price)}
                      {idx === 0 && (
                        <span className="ml-2 rounded-full bg-orange-100 px-2 py-0.5 text-[10px] font-semibold text-orange-600">
                          atual
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-2 text-slate-500">
                      {new Date(entry.effectiveFrom).toLocaleDateString('pt-BR', {
                        day: '2-digit',
                        month: '2-digit',
                        year: 'numeric',
                        hour: '2-digit',
                        minute: '2-digit',
                      })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </DialogContent>
    </Dialog>
  )
}
