import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Download } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { TopBar } from '@/components/layout/TopBar'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getSalesReport } from '@/api/reports'
import { formatBRL, formatLiters } from '@/lib/utils'
import { PAYMENT_METHOD_LABELS, type PaymentMethod } from '@/types'

function today() {
  return new Date().toISOString().slice(0, 10)
}
function weekAgo() {
  const d = new Date()
  d.setDate(d.getDate() - 7)
  return d.toISOString().slice(0, 10)
}

export function RelatorioVendasPage() {
  const { station } = useActiveStation()
  const [from, setFrom] = useState(weekAgo)
  const [to, setTo] = useState(today)

  const { data, isLoading } = useQuery({
    queryKey: ['report-sales', station?.id, from, to],
    queryFn: () => getSalesReport(station!.id, from, to),
    enabled: !!station && !!from && !!to,
  })

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center text-sm text-slate-400">
        Selecione um posto.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Relatório de Vendas" />
      <div className="flex flex-1 flex-col gap-6 overflow-auto p-6">
        <div className="flex items-end gap-3">
          <div>
            <label className="text-[10px] uppercase text-slate-400">De</label>
            <Input type="date" value={from} onChange={e => setFrom(e.target.value)} className="w-40" />
          </div>
          <div>
            <label className="text-[10px] uppercase text-slate-400">Até</label>
            <Input type="date" value={to} onChange={e => setTo(e.target.value)} className="w-40" />
          </div>
          {data && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => window.open(`/api/reports/sales/csv?stationId=${station.id}&from=${from}&to=${to}`, '_blank')}
            >
              <Download size={14} className="mr-1" /> Exportar CSV
            </Button>
          )}
        </div>

        {isLoading ? (
          <div className="flex flex-col gap-3">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-48 w-full" />
          </div>
        ) : data ? (
          <>
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-3">
              <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
                <p className="text-[10px] uppercase text-slate-400">Receita total</p>
                <p className="mt-1 text-xl font-bold text-slate-900 dark:text-slate-100">{formatBRL(data.totalRevenue)}</p>
              </div>
              <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
                <p className="text-[10px] uppercase text-slate-400">Litros</p>
                <p className="mt-1 text-xl font-bold text-slate-900 dark:text-slate-100">{formatLiters(data.totalLiters)}</p>
              </div>
              <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
                <p className="text-[10px] uppercase text-slate-400">Abastecimentos</p>
                <p className="mt-1 text-xl font-bold text-slate-900 dark:text-slate-100">{data.totalCount}</p>
              </div>
            </div>

            <div className="rounded-lg border bg-white dark:bg-slate-900 overflow-hidden">
              <h3 className="px-4 py-3 text-xs font-semibold uppercase text-slate-400 border-b dark:border-slate-700">
                Resumo diário
              </h3>
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b bg-slate-50 dark:bg-slate-800">
                    <th className="px-4 py-2 text-left text-slate-400">Data</th>
                    <th className="px-4 py-2 text-right text-slate-400">Receita</th>
                    <th className="px-4 py-2 text-right text-slate-400">Litros</th>
                    <th className="px-4 py-2 text-right text-slate-400">Qtd</th>
                  </tr>
                </thead>
                <tbody>
                  {data.daily.map(d => (
                    <tr key={d.date} className="border-b last:border-0">
                      <td className="px-4 py-2 text-slate-700 dark:text-slate-300">{new Date(d.date + 'T12:00:00').toLocaleDateString('pt-BR')}</td>
                      <td className="px-4 py-2 text-right font-medium text-slate-700 dark:text-slate-300">{formatBRL(d.revenue)}</td>
                      <td className="px-4 py-2 text-right text-slate-500">{formatLiters(d.liters)}</td>
                      <td className="px-4 py-2 text-right text-slate-500">{d.count}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            <div className="grid gap-6 md:grid-cols-2">
              <div className="rounded-lg border bg-white dark:bg-slate-900 overflow-hidden">
                <h3 className="px-4 py-3 text-xs font-semibold uppercase text-slate-400 border-b dark:border-slate-700">
                  Por combustível
                </h3>
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b bg-slate-50 dark:bg-slate-800">
                      <th className="px-4 py-2 text-left text-slate-400">Combustível</th>
                      <th className="px-4 py-2 text-right text-slate-400">Receita</th>
                      <th className="px-4 py-2 text-right text-slate-400">Litros</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.byFuel.map(f => (
                      <tr key={f.fuelName} className="border-b last:border-0">
                        <td className="px-4 py-2 text-slate-700 dark:text-slate-300">{f.fuelName}</td>
                        <td className="px-4 py-2 text-right font-medium text-slate-700 dark:text-slate-300">{formatBRL(f.revenue)}</td>
                        <td className="px-4 py-2 text-right text-slate-500">{formatLiters(f.liters)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="rounded-lg border bg-white dark:bg-slate-900 overflow-hidden">
                <h3 className="px-4 py-3 text-xs font-semibold uppercase text-slate-400 border-b dark:border-slate-700">
                  Por forma de pagamento
                </h3>
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b bg-slate-50 dark:bg-slate-800">
                      <th className="px-4 py-2 text-left text-slate-400">Método</th>
                      <th className="px-4 py-2 text-right text-slate-400">Qtd</th>
                      <th className="px-4 py-2 text-right text-slate-400">Valor</th>
                    </tr>
                  </thead>
                  <tbody>
                    {data.byPayment.map(p => (
                      <tr key={p.paymentMethod} className="border-b last:border-0">
                        <td className="px-4 py-2 text-slate-700 dark:text-slate-300">
                          {PAYMENT_METHOD_LABELS[p.paymentMethod as PaymentMethod] ?? p.paymentMethod}
                        </td>
                        <td className="px-4 py-2 text-right text-slate-500">{p.count}</td>
                        <td className="px-4 py-2 text-right font-medium text-slate-700 dark:text-slate-300">{formatBRL(p.revenue)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </>
        ) : null}
      </div>
    </div>
  )
}
