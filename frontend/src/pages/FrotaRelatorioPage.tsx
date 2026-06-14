import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Download, Search } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { TopBar } from '@/components/layout/TopBar'
import { OdometerAlert } from '@/components/frota/OdometerAlert'
import { getFleetClients } from '@/api/fleet-clients'
import { getFleetConsumptionReport, downloadFleetCsv } from '@/api/fleet-reports'
import { useActiveStation } from '@/hooks/useActiveStation'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'

function today() {
  return new Date().toISOString().slice(0, 10)
}

function firstDayOfMonth() {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-01`
}

export function FrotaRelatorioPage() {
  const { station } = useActiveStation()

  const [clientId, setClientId] = useState<string>('')
  const [from, setFrom] = useState(firstDayOfMonth())
  const [to, setTo] = useState(today())
  const [enabled, setEnabled] = useState(false)

  const { data: clients = [] } = useQuery({
    queryKey: ['fleet-clients', station?.id],
    queryFn: () => getFleetClients(station!.id),
    enabled: !!station,
  })

  const { data: report, isLoading, isFetching } = useQuery({
    queryKey: ['fleet-report', station?.id, clientId, from, to],
    queryFn: () =>
      getFleetConsumptionReport({
        stationId: station!.id,
        clientId: clientId || undefined,
        from,
        to,
      }),
    enabled: enabled && !!station,
  })

  function handleSearch() {
    setEnabled(true)
  }

  function handleExportCsv() {
    if (!station) return
    const url = downloadFleetCsv({
      stationId: station.id,
      clientId: clientId || undefined,
      from,
      to,
    })
    window.open(url, '_blank')
  }

  if (!station) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Frota — Relatório" />
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-slate-400">Selecione um posto ativo na barra lateral.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Frota — Relatório" subtitle="Consumo por período" />

      <div className="flex-1 overflow-auto p-6">
        {/* Filtros */}
        <div className="mb-6 flex flex-wrap items-end gap-3 rounded-lg border bg-white p-4 dark:bg-slate-900">
          <div className="w-60">
            <Label>Cliente (opcional)</Label>
            <Select value={clientId} onValueChange={setClientId}>
              <SelectTrigger>
                <SelectValue placeholder="Todos os clientes" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="">Todos os clientes</SelectItem>
                {clients.map((c) => (
                  <SelectItem key={c.id} value={c.id}>
                    {c.companyName}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          <div>
            <Label>Data início</Label>
            <Input type="date" value={from} onChange={(e) => { setFrom(e.target.value); setEnabled(false) }} />
          </div>

          <div>
            <Label>Data fim</Label>
            <Input type="date" value={to} onChange={(e) => { setTo(e.target.value); setEnabled(false) }} />
          </div>

          <Button
            className="bg-orange-600 hover:bg-orange-700"
            onClick={handleSearch}
            disabled={isFetching}
          >
            <Search size={14} className="mr-1" />
            {isFetching ? 'Buscando…' : 'Buscar'}
          </Button>

          {report && (
            <Button variant="outline" onClick={handleExportCsv}>
              <Download size={14} className="mr-1" />
              Exportar CSV
            </Button>
          )}
        </div>

        {/* Summary */}
        {isLoading && enabled && (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-20 w-full" />
            <Skeleton className="h-48 w-full" />
          </div>
        )}

        {report && (
          <>
            <div className="mb-6 grid grid-cols-2 gap-3 sm:grid-cols-4">
              <div className="rounded-lg border bg-white p-4 dark:bg-slate-900">
                <p className="text-xs text-slate-400 uppercase font-semibold">Volume total</p>
                <p className="mt-1 text-2xl font-bold text-slate-800 dark:text-slate-100">
                  {report.summary.totalLiters.toFixed(3)} L
                </p>
              </div>
              <div className="rounded-lg border bg-white p-4 dark:bg-slate-900">
                <p className="text-xs text-slate-400 uppercase font-semibold">Valor total</p>
                <p className="mt-1 text-2xl font-bold text-slate-800 dark:text-slate-100">
                  R$ {report.summary.totalAmount.toFixed(2)}
                </p>
              </div>
              <div className="rounded-lg border bg-white p-4 dark:bg-slate-900">
                <p className="text-xs text-slate-400 uppercase font-semibold">Abastecimentos</p>
                <p className="mt-1 text-2xl font-bold text-slate-800 dark:text-slate-100">
                  {report.summary.count}
                </p>
              </div>
              <div className="rounded-lg border bg-white p-4 dark:bg-slate-900">
                <p className="text-xs text-slate-400 uppercase font-semibold">Alertas hodômetro</p>
                <p className={`mt-1 text-2xl font-bold ${report.summary.odometerAlerts > 0 ? 'text-yellow-600' : 'text-slate-800 dark:text-slate-100'}`}>
                  {report.summary.odometerAlerts}
                </p>
              </div>
            </div>

            {report.lines.length === 0 ? (
              <p className="text-sm text-slate-400">Nenhum abastecimento no período.</p>
            ) : (
              <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b bg-slate-50 dark:bg-slate-800">
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Data/Hora</th>
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Motorista</th>
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Placa</th>
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Combustível</th>
                      <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Litros</th>
                      <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Total</th>
                      <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">Hodômetro</th>
                    </tr>
                  </thead>
                  <tbody>
                    {report.lines.map((line, idx) => (
                      <tr key={idx} className="border-b last:border-0">
                        <td className="px-4 py-3 text-slate-500 whitespace-nowrap">
                          {new Date(line.fueledAt).toLocaleString('pt-BR')}
                        </td>
                        <td className="px-4 py-3 text-slate-800 dark:text-slate-200">
                          {line.driverName}
                          <span className="block text-xs text-slate-400">{line.driverCpf}</span>
                        </td>
                        <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">
                          {line.vehiclePlate}
                          {line.vehicleModel && (
                            <span className="block text-xs font-normal text-slate-400">{line.vehicleModel}</span>
                          )}
                        </td>
                        <td className="px-4 py-3 text-slate-500">{line.fuelName}</td>
                        <td className="px-4 py-3 text-right text-slate-800 dark:text-slate-200">
                          {line.liters.toFixed(3)}
                        </td>
                        <td className="px-4 py-3 text-right font-semibold text-slate-800 dark:text-slate-200">
                          R$ {line.totalAmount.toFixed(2)}
                        </td>
                        <td className="px-4 py-3">
                          {line.odometerAlert ? (
                            <OdometerAlert current={line.odometer} />
                          ) : (
                            <span className="text-slate-500">{line.odometer} km</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </>
        )}

        {!enabled && !isLoading && (
          <p className="text-sm text-slate-400">
            Defina os filtros e clique em "Buscar" para gerar o relatório.
          </p>
        )}
      </div>
    </div>
  )
}
