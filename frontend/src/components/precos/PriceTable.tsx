import { useState } from 'react'
import { Skeleton } from '@/components/ui/skeleton'
import { PriceHistoryModal } from './PriceHistoryModal'
import { formatBRL } from '@/lib/utils'
import type { FuelPrice } from '@/types'

type Props = {
  prices: FuelPrice[]
  isLoading: boolean
}

export function PriceTable({ prices, isLoading }: Props) {
  const [historyModal, setHistoryModal] = useState<{ fuelId: string; fuelName: string } | null>(
    null,
  )

  if (isLoading) {
    return (
      <div className="flex flex-col gap-2">
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-10 w-full" />
        <Skeleton className="h-10 w-full" />
      </div>
    )
  }

  if (prices.length === 0) {
    return <p className="text-sm text-slate-400">Nenhum preço cadastrado.</p>
  }

  return (
    <>
      <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-slate-50 dark:bg-slate-800">
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                Combustível
              </th>
              <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                Preço/L
              </th>
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                Desde
              </th>
              <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                Histórico
              </th>
            </tr>
          </thead>
          <tbody>
            {prices.map((p) => (
              <tr key={p.id} className="border-b last:border-0">
                <td className="px-4 py-2.5 font-semibold text-slate-800 dark:text-slate-200">{p.fuelName}</td>
                <td className="px-4 py-2.5 text-right font-bold text-orange-600 tabular-nums">
                  {formatBRL(p.price)}
                </td>
                <td className="px-4 py-2.5 text-slate-400 dark:text-slate-500">
                  {new Date(p.effectiveFrom).toLocaleDateString('pt-BR')}
                </td>
                <td className="px-4 py-2.5 text-center">
                  <button
                    onClick={() => setHistoryModal({ fuelId: p.fuelId, fuelName: p.fuelName })}
                    className="text-xs font-medium text-blue-500 hover:text-blue-700"
                  >
                    Ver ›
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {historyModal && (
        <PriceHistoryModal
          fuelId={historyModal.fuelId}
          fuelName={historyModal.fuelName}
          open={!!historyModal}
          onOpenChange={(open) => !open && setHistoryModal(null)}
        />
      )}
    </>
  )
}
