import { useQuery } from '@tanstack/react-query'
import { Fuel, DollarSign, Wrench, Truck } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getDashboard } from '@/api/dashboard'
import { formatBRL, formatLiters } from '@/lib/utils'
import { PAYMENT_METHOD_LABELS, type PaymentMethod } from '@/types'

function KpiCard({ label, value, icon }: { label: string; value: string; icon: React.ReactNode }) {
  return (
    <div className="flex items-center gap-3 rounded-lg border bg-white dark:bg-slate-900 p-4">
      <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-orange-50 dark:bg-orange-900/20 text-orange-600">
        {icon}
      </div>
      <div>
        <p className="text-[10px] uppercase tracking-wider text-slate-400 dark:text-slate-500">{label}</p>
        <p className="text-lg font-bold text-slate-900 dark:text-slate-100">{value}</p>
      </div>
    </div>
  )
}

export function DashboardPage() {
  const { station } = useActiveStation()

  const { data, isLoading } = useQuery({
    queryKey: ['dashboard', station?.id],
    queryFn: () => getDashboard(station!.id),
    enabled: !!station,
    refetchInterval: 30_000,
  })

  if (!station) {
    return (
      <div className="flex flex-1 flex-col items-center justify-center gap-4 text-slate-400">
        <Fuel size={40} />
        <p className="text-sm">Selecione um posto na barra lateral para continuar.</p>
      </div>
    )
  }

  if (isLoading || !data) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Dashboard" />
        <div className="p-6 flex flex-col gap-4">
          <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
            {[1, 2, 3, 4].map(i => <Skeleton key={i} className="h-20" />)}
          </div>
          <Skeleton className="h-64" />
        </div>
      </div>
    )
  }

  const maxFuelRevenue = Math.max(...data.revenueByFuel.map(f => f.revenue), 1)

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title="Dashboard"
        actions={
          data.activeShift ? (
            <span className="rounded-full bg-green-100 px-3 py-1 text-xs font-semibold text-green-700">
              ● Turno aberto · {data.activeShift.employeeName}
            </span>
          ) : (
            <span className="rounded-full bg-slate-100 dark:bg-slate-800 px-3 py-1 text-xs text-slate-500">
              Sem turno aberto
            </span>
          )
        }
      />

      <div className="flex flex-1 flex-col gap-6 overflow-auto p-6">
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
          <KpiCard label="Receita hoje" value={formatBRL(data.totalRevenue)} icon={<DollarSign size={18} />} />
          <KpiCard label="Litros hoje" value={formatLiters(data.totalLiters)} icon={<Fuel size={18} />} />
          <KpiCard label="Abastecimentos" value={String(data.fuelingCount)} icon={<Fuel size={18} />} />
          <KpiCard label="OS abertas" value={String(data.openServiceOrders)} icon={<Wrench size={18} />} />
        </div>

        <div className="grid gap-6 md:grid-cols-2">
          <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
            <h3 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400 dark:text-slate-500">
              Receita por combustível
            </h3>
            {data.revenueByFuel.length === 0 ? (
              <p className="text-sm text-slate-400">Nenhum dado.</p>
            ) : (
              <div className="flex flex-col gap-2">
                {data.revenueByFuel.map(f => (
                  <div key={f.fuelName} className="flex flex-col gap-1">
                    <div className="flex justify-between text-xs">
                      <span className="font-medium text-slate-700 dark:text-slate-300">{f.fuelName}</span>
                      <span className="text-slate-500">{formatBRL(f.revenue)}</span>
                    </div>
                    <div className="h-2 rounded-full bg-slate-100 dark:bg-slate-800">
                      <div
                        className="h-2 rounded-full bg-orange-500"
                        style={{ width: `${(f.revenue / maxFuelRevenue) * 100}%` }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
            <h3 className="mb-3 text-xs font-semibold uppercase tracking-wider text-slate-400 dark:text-slate-500">
              Formas de pagamento
            </h3>
            {data.revenueByPayment.length === 0 ? (
              <p className="text-sm text-slate-400">Nenhum dado.</p>
            ) : (
              <div className="overflow-hidden rounded border dark:border-slate-700">
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b bg-slate-50 dark:bg-slate-800">
                      <th className="px-3 py-2 text-left text-slate-400">Método</th>
                      <th className="px-3 py-2 text-right text-slate-400">Qtd</th>
                      <th className="px-3 py-2 text-right text-slate-400">Valor</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.revenueByPayment.map(p => (
                      <tr key={p.paymentMethod} className="border-b last:border-0">
                        <td className="px-3 py-2 text-slate-700 dark:text-slate-300">
                          {PAYMENT_METHOD_LABELS[p.paymentMethod as PaymentMethod] ?? p.paymentMethod}
                        </td>
                        <td className="px-3 py-2 text-right text-slate-500">{p.count}</td>
                        <td className="px-3 py-2 text-right font-medium text-slate-700 dark:text-slate-300">
                          {formatBRL(p.revenue)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        {data.fleetFuelingCount > 0 && (
          <div className="flex items-center gap-2 rounded-lg border bg-white dark:bg-slate-900 p-4">
            <Truck size={16} className="text-slate-400" />
            <span className="text-sm text-slate-600 dark:text-slate-400">
              <strong>{data.fleetFuelingCount}</strong> abastecimentos frota hoje
            </span>
          </div>
        )}
      </div>
    </div>
  )
}
