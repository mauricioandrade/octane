import { useState } from 'react'
import { Download } from 'lucide-react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useQuery, useQueries, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from '@/components/ui/sheet'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Badge } from '@/components/ui/badge'
import { cn } from '@/lib/utils'
import { formatLiters } from '@/lib/utils'
import { registerReading } from '@/api/readings'
import { closeShift, getReconciliation } from '@/api/shifts'
import { getPumps } from '@/api/pumps'
import { getNozzles } from '@/api/nozzles'
import { getFuels } from '@/api/fuels'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { ShiftReconciliation, ReconciliationLine } from '@/types'
import { exportReconciliationCSV } from '@/lib/export'

const schema = z.object({
  readings: z.record(z.string(), z.coerce.number().min(0, 'Obrigatório')),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  shiftId: string
  employeeName: string
}

function divergenceBadge(line: ReconciliationLine) {
  const div = Math.abs(line.divergenceLiters)
  if (div === 0) {
    return <Badge className="bg-green-100 text-green-700 hover:bg-green-100">0 L</Badge>
  }
  const pct = line.measuredLiters > 0 ? (div / line.measuredLiters) * 100 : 0
  if (pct <= 0.6) {
    return (
      <Badge className="bg-yellow-100 text-yellow-700 hover:bg-yellow-100">
        {formatLiters(div)} ({pct.toFixed(2)}%)
      </Badge>
    )
  }
  return (
    <Badge className="bg-red-100 text-red-700 hover:bg-red-100">
      {formatLiters(div)} ({pct.toFixed(2)}%)
    </Badge>
  )
}

export function CloseShiftSheet({ open, onOpenChange, shiftId, employeeName }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const [step, setStep] = useState<1 | 2>(1)
  const [reconciliation, setReconciliation] = useState<ShiftReconciliation | null>(null)
  const [calculating, setCalculating] = useState(false)

  const { data: pumps = [] } = useQuery({
    queryKey: ['pumps', station?.id, 'ACTIVE'],
    queryFn: () => getPumps(station!.id, 'ACTIVE'),
    enabled: !!station && open,
  })

  const nozzleResults = useQueries({
    queries: pumps.map((pump) => ({
      queryKey: ['nozzles', pump.id, 'active'],
      queryFn: () => getNozzles(pump.id, true),
      enabled: pumps.length > 0 && open,
    })),
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
    enabled: open,
  })

  const activeNozzles = nozzleResults.flatMap((q) => q.data ?? [])
  const fuelById = Object.fromEntries(fuels.map((f) => [f.id, f]))

  const { register, handleSubmit, formState: { errors } } = useForm<FormData>({
    resolver: zodResolver(schema) as any,
  })

  async function onCalculate(data: FormData) {
    setCalculating(true)
    try {
      let readingFailed = false
      for (const nozzle of activeNozzles) {
        const totalizer = data.readings[nozzle.id]
        if (totalizer === undefined) continue
        try {
          await registerReading(shiftId, {
            nozzleId: nozzle.id,
            type: 'CLOSING',
            totalizer,
          })
        } catch {
          toast.error(`Erro ao registrar encerrante do bico ${nozzle.number}`)
          readingFailed = true
        }
      }
      if (readingFailed) {
        setCalculating(false)
        return
      }
      // Fecha o turno (grava reconciliação atomicamente no backend)
      await closeShift(shiftId)
      qc.invalidateQueries({ queryKey: ['shift', 'open', station?.id] })
      const rec = await getReconciliation(shiftId)
      setReconciliation(rec)
      setStep(2)
    } catch {
      toast.error('Erro ao fechar turno')
    } finally {
      setCalculating(false)
    }
  }

  function handleClose() {
    onOpenChange(false)
    setStep(1)
    setReconciliation(null)
  }

  return (
    <Sheet open={open} onOpenChange={handleClose}>
      <SheetContent className="w-[480px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>
            Fechar turno — {employeeName}
          </SheetTitle>
        </SheetHeader>

        {/* Indicador de etapa */}
        <div className="mt-3 mb-4 flex gap-2">
          <span className={cn('rounded-full px-3 py-1 text-xs font-semibold', step === 1 ? 'bg-orange-600 text-white' : 'bg-slate-100 text-slate-400')}>
            1. Encerrantes
          </span>
          <span className={cn('rounded-full px-3 py-1 text-xs font-semibold', step === 2 ? 'bg-orange-600 text-white' : 'bg-slate-100 text-slate-400')}>
            2. Reconciliação
          </span>
        </div>

        {/* Etapa 1: encerrantes */}
        {step === 1 && (
          <form onSubmit={handleSubmit(onCalculate)} className="flex flex-col gap-4">
            <p className="text-sm text-slate-500">
              Informe a leitura do encerrante de fechamento para cada bico.
            </p>
            {activeNozzles.map((nozzle) => (
              <div key={nozzle.id}>
                <Label htmlFor={`c-${nozzle.id}`}>
                  Bico {nozzle.number} — {fuelById[nozzle.fuelId]?.name ?? '…'}
                </Label>
                <Input
                  id={`c-${nozzle.id}`}
                  type="number"
                  step="0.001"
                  min="0"
                  placeholder="0.000"
                  defaultValue="0"
                  {...register(`readings.${nozzle.id}`)}
                />
                {errors.readings?.[nozzle.id] && (
                  <p className="mt-1 text-xs text-red-500">
                    {errors.readings[nozzle.id]?.message}
                  </p>
                )}
              </div>
            ))}
            <Button
              type="submit"
              disabled={calculating}
              className="mt-2 bg-orange-600 hover:bg-orange-700"
            >
              {calculating ? 'Calculando…' : 'Calcular reconciliação →'}
            </Button>
          </form>
        )}

        {/* Etapa 2: reconciliação */}
        {step === 2 && reconciliation && (
          <div className="flex flex-col gap-4">
            <p className="text-sm text-slate-500">
              Turno fechado. Reconciliação ANP 884/2022:
            </p>

            <div className="overflow-hidden rounded-lg border">
              <table className="w-full text-xs">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="px-3 py-2 text-left font-semibold text-slate-500">Bico</th>
                    <th className="px-3 py-2 text-right font-semibold text-slate-500">Medido</th>
                    <th className="px-3 py-2 text-right font-semibold text-slate-500">Lançado</th>
                    <th className="px-3 py-2 text-right font-semibold text-slate-500">Divergência</th>
                  </tr>
                </thead>
                <tbody>
                  {reconciliation.lines.map((line) => (
                    <tr key={line.nozzleId} className="border-t">
                      <td className="px-3 py-2 font-medium">
                        B{line.nozzleNumber} · {line.fuelName}
                      </td>
                      <td className="px-3 py-2 text-right tabular-nums">
                        {formatLiters(line.measuredLiters)}
                      </td>
                      <td className="px-3 py-2 text-right tabular-nums">
                        {formatLiters(line.fueledLiters)}
                      </td>
                      <td className="px-3 py-2 text-right">
                        {divergenceBadge(line)}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="border-t bg-slate-50">
                  <tr>
                    <td className="px-3 py-2 font-semibold" colSpan={2}>Total medido</td>
                    <td className="px-3 py-2 text-right font-semibold tabular-nums" colSpan={2}>
                      {formatLiters(reconciliation.totalMeasuredLiters)}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>

            <Button
              variant="outline"
              size="sm"
              className="w-full"
              onClick={() => exportReconciliationCSV(reconciliation, `reconciliacao-${shiftId.slice(0, 8)}.csv`)}
            >
              <Download size={14} className="mr-1" />
              Exportar CSV
            </Button>

            <Button onClick={handleClose} className="w-full bg-slate-700 hover:bg-slate-800">
              Concluído
            </Button>
          </div>
        )}
      </SheetContent>
    </Sheet>
  )
}
