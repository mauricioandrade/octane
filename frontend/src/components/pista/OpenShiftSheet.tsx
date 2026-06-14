import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useQuery, useQueryClient, useQueries } from '@tanstack/react-query'
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
import { useActiveStation } from '@/hooks/useActiveStation'
import { openShift } from '@/api/shifts'
import { registerReading } from '@/api/readings'
import { getPumps } from '@/api/pumps'
import { getNozzles } from '@/api/nozzles'
import { getFuels } from '@/api/fuels'

const schema = z.object({
  employeeName: z.string().min(1, 'Obrigatório').max(100),
  readings: z.record(z.string(), z.coerce.number().min(0, 'Obrigatório')),
})

// z.coerce.number() in zod v4 infers _input as unknown; define output type explicitly
type FormData = {
  employeeName: string
  readings: Record<string, number>
}

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function OpenShiftSheet({ open, onOpenChange }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()

  const { data: pumps = [] } = useQuery({
    queryKey: ['pumps', station?.id, 'ACTIVE'],
    queryFn: () => getPumps(station!.id, 'ACTIVE'),
    enabled: !!station && open,
  })

  const nozzleQueries = useQueries({
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

  const activeNozzles = nozzleQueries.flatMap((q) => q.data ?? [])
  const fuelById = Object.fromEntries(fuels.map((f) => [f.id, f]))

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } = useForm<FormData>({ resolver: zodResolver(schema) as any })

  const [submitting, setSubmitting] = useState(false)

  async function onSubmit(data: FormData) {
    if (!station) return
    setSubmitting(true)
    try {
      const shift = await openShift({
        stationId: station.id,
        employeeName: data.employeeName,
      })

      let readingError = false
      for (const nozzle of activeNozzles) {
        const totalizer = data.readings[nozzle.id]
        if (totalizer === undefined) continue
        try {
          await registerReading(shift.id, {
            nozzleId: nozzle.id,
            type: 'OPENING',
            totalizer,
          })
        } catch {
          toast.error(`Erro ao registrar encerrante do bico ${nozzle.number}`)
          readingError = true
        }
      }

      if (!readingError) {
        toast.success('Turno aberto!')
        qc.invalidateQueries({ queryKey: ['shift', 'open', station.id] })
        reset()
        onOpenChange(false)
      }
    } catch (err) {
      toast.error('Erro ao abrir turno')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[400px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>Abrir turno</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit(onSubmit)} className="mt-4 flex flex-col gap-4">
          <div>
            <Label htmlFor="employeeName">Nome do frentista</Label>
            <Input
              id="employeeName"
              placeholder="Ex: João Silva"
              {...register('employeeName')}
            />
            {errors.employeeName && (
              <p className="mt-1 text-xs text-red-500">{errors.employeeName.message}</p>
            )}
          </div>

          {activeNozzles.length > 0 && (
            <div>
              <p className="mb-2 text-sm font-semibold text-slate-700">
                Encerrantes de abertura
              </p>
              <div className="flex flex-col gap-3">
                {activeNozzles.map((nozzle) => (
                  <div key={nozzle.id}>
                    <Label htmlFor={`r-${nozzle.id}`}>
                      Bico {nozzle.number} — {fuelById[nozzle.fuelId]?.name ?? '…'}
                    </Label>
                    <Input
                      id={`r-${nozzle.id}`}
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
              </div>
            </div>
          )}

          <Button type="submit" disabled={submitting} className="mt-2 bg-orange-600 hover:bg-orange-700">
            {submitting ? 'Abrindo…' : 'Abrir turno'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
