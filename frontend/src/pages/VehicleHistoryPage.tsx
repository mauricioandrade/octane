import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { ServiceOrderStatusBadge } from '@/components/os/ServiceOrderStatusBadge'
import { getVehicleHistory } from '@/api/service-orders'
import { formatBRL } from '@/lib/utils'

export function VehicleHistoryPage() {
  const [plateInput, setPlateInput] = useState('')
  const [searchPlate, setSearchPlate] = useState('')

  const { data: orders = [], isLoading, isFetched } = useQuery({
    queryKey: ['vehicle-history', searchPlate],
    queryFn: () => getVehicleHistory(searchPlate.toUpperCase().trim()),
    enabled: !!searchPlate,
  })

  function handleSearch() {
    const trimmed = plateInput.trim().toUpperCase()
    if (trimmed) setSearchPlate(trimmed)
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === 'Enter') handleSearch()
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Histórico por Placa" subtitle="Consulte ordens de serviço por veículo" />

      <div className="flex-1 overflow-auto p-6">
        {/* Search bar */}
        <div className="mb-6 flex items-center gap-2">
          <Input
            value={plateInput}
            onChange={(e) => setPlateInput(e.target.value.toUpperCase())}
            onKeyDown={handleKeyDown}
            placeholder="Digite a placa (ex: ABC1234)"
            className="w-56"
          />
          <Button
            size="sm"
            className="bg-orange-600 hover:bg-orange-700"
            onClick={handleSearch}
            disabled={!plateInput.trim()}
          >
            <Search size={14} className="mr-1" />
            Buscar
          </Button>
        </div>

        {/* Results */}
        {!searchPlate && (
          <p className="text-sm text-slate-400">
            Informe uma placa para consultar o histórico de ordens de serviço.
          </p>
        )}

        {isLoading && (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-20 w-full" />
          </div>
        )}

        {!isLoading && isFetched && searchPlate && orders.length === 0 && (
          <p className="text-sm text-slate-400">
            Nenhuma ordem de serviço encontrada para a placa{' '}
            <span className="font-semibold">{searchPlate}</span>.
          </p>
        )}

        {orders.length > 0 && (
          <div className="flex flex-col gap-3">
            <p className="text-sm text-slate-500 dark:text-slate-400">
              {orders.length} OS encontrada{orders.length !== 1 ? 's' : ''} para{' '}
              <span className="font-semibold text-slate-700 dark:text-slate-200">{searchPlate}</span>
            </p>
            <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b bg-slate-50 dark:bg-slate-800">
                    <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                      Posto
                    </th>
                    <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                      Hodômetro
                    </th>
                    <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                      Itens
                    </th>
                    <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                      Total
                    </th>
                    <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                      Status
                    </th>
                    <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                      Data
                    </th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((order) => (
                    <tr key={order.id} className="border-b last:border-0">
                      <td className="px-4 py-3 text-slate-700 dark:text-slate-300">
                        {order.stationName}
                      </td>
                      <td className="px-4 py-3 text-right text-slate-500 dark:text-slate-400">
                        {order.odometer.toLocaleString('pt-BR')} km
                      </td>
                      <td className="px-4 py-3 text-right text-slate-500 dark:text-slate-400">
                        {order.items.length}
                      </td>
                      <td className="px-4 py-3 text-right font-semibold text-slate-800 dark:text-slate-200">
                        {formatBRL(order.totalAmount)}
                      </td>
                      <td className="px-4 py-3 text-center">
                        <ServiceOrderStatusBadge status={order.status} />
                      </td>
                      <td className="px-4 py-3 text-slate-500 dark:text-slate-400">
                        {new Date(order.openedAt).toLocaleString('pt-BR')}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
