import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { NewPriceForm } from '@/components/precos/NewPriceForm'
import { PriceTable } from '@/components/precos/PriceTable'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getCurrentPrices } from '@/api/prices'
import { getFuels } from '@/api/fuels'

export function PrecosPage() {
  const { station } = useActiveStation()

  const { data: prices = [], isLoading: loadingPrices } = useQuery({
    queryKey: ['prices', station?.id],
    queryFn: () => getCurrentPrices(station!.id),
    enabled: !!station,
  })

  const { data: fuels = [], isLoading: loadingFuels } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
  })

  if (!station) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Preços" subtitle="Preços de combustíveis por posto" />
        <div className="flex flex-1 items-center justify-center p-6">
          <p className="text-sm text-slate-400">Selecione um posto para gerenciar preços.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Preços" subtitle={`Preços vigentes — ${station.name}`} />

      <div className="flex flex-1 overflow-hidden">
        <div className="w-72 shrink-0 border-r p-6">
          {loadingFuels ? (
            <div className="flex flex-col gap-3">
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-8 w-full" />
              <Skeleton className="h-10 w-full" />
            </div>
          ) : (
            <NewPriceForm fuels={fuels} currentPrices={prices} />
          )}
        </div>

        <div className="flex-1 overflow-auto p-6">
          <PriceTable prices={prices} isLoading={loadingPrices} />
        </div>
      </div>
    </div>
  )
}
