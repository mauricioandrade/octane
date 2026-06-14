import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { ServiceOrderStatusBadge } from '@/components/os/ServiceOrderStatusBadge'
import { ServiceOrderSheet } from '@/components/os/ServiceOrderSheet'
import { listServiceOrders } from '@/api/service-orders'
import { useActiveStation } from '@/hooks/useActiveStation'
import { formatBRL } from '@/lib/utils'

export function OrdensServicoPage() {
  const { station } = useActiveStation()
  const navigate = useNavigate()

  const [sheetOpen, setSheetOpen] = useState(false)
  const [statusFilter, setStatusFilter] = useState<string>('')
  const [fromFilter, setFromFilter] = useState('')
  const [toFilter, setToFilter] = useState('')

  const { data: orders = [], isLoading } = useQuery({
    queryKey: ['service-orders', station?.id, statusFilter, fromFilter, toFilter],
    queryFn: () =>
      listServiceOrders(station!.id, {
        status: statusFilter || undefined,
        from: fromFilter || undefined,
        to: toFilter || undefined,
      }),
    enabled: !!station,
  })

  if (!station) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Ordens de Serviço" />
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-slate-400">Selecione um posto ativo na barra lateral.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Ordens de Serviço" subtitle="Gerenciamento de OS do posto" />

      <div className="flex-1 overflow-auto p-6">
        {/* Toolbar */}
        <div className="mb-4 flex flex-wrap items-center gap-3">
          <Select value={statusFilter} onValueChange={setStatusFilter}>
            <SelectTrigger className="w-40">
              <SelectValue placeholder="Todas" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="">Todas</SelectItem>
              <SelectItem value="OPEN">Abertas</SelectItem>
              <SelectItem value="CLOSED">Fechadas</SelectItem>
              <SelectItem value="CANCELLED">Canceladas</SelectItem>
            </SelectContent>
          </Select>

          <div className="flex items-center gap-2">
            <Input
              type="date"
              value={fromFilter}
              onChange={(e) => setFromFilter(e.target.value)}
              className="w-40"
              placeholder="De"
            />
            <span className="text-sm text-slate-400">até</span>
            <Input
              type="date"
              value={toFilter}
              onChange={(e) => setToFilter(e.target.value)}
              className="w-40"
              placeholder="Até"
            />
          </div>

          <div className="ml-auto">
            <Button
              size="sm"
              className="bg-orange-600 hover:bg-orange-700"
              onClick={() => setSheetOpen(true)}
            >
              <Plus size={14} className="mr-1" />
              Nova OS
            </Button>
          </div>
        </div>

        {/* Table */}
        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : orders.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhuma ordem de serviço encontrada.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Placa
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Cliente
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
                    Abertura
                  </th>
                </tr>
              </thead>
              <tbody>
                {orders.map((order) => (
                  <tr
                    key={order.id}
                    className="cursor-pointer border-b last:border-0 hover:bg-slate-50 dark:hover:bg-slate-800"
                    onClick={() => navigate(`/os/${order.id}`)}
                  >
                    <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">
                      {order.plate}
                    </td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">
                      {order.customerName ?? <span className="text-slate-300 dark:text-slate-600">—</span>}
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
        )}
      </div>

      <ServiceOrderSheet open={sheetOpen} onOpenChange={setSheetOpen} />
    </div>
  )
}
