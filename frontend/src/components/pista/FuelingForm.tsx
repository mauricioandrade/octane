import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { X } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { cn } from '@/lib/utils'
import { formatBRL } from '@/lib/utils'
import { registerFueling } from '@/api/fuelings'
import { getCurrentPrices } from '@/api/prices'
import { useActiveStation } from '@/hooks/useActiveStation'
import { PAYMENT_METHOD_LABELS, type PaymentMethod } from '@/types'

const PAYMENT_METHODS = Object.entries(PAYMENT_METHOD_LABELS) as [PaymentMethod, string][]

const schema = z.object({
  liters: z.coerce.number({ error: 'Obrigatório' }).positive('Deve ser maior que 0'),
  paymentMethod: z.string().min(1, 'Selecione uma forma de pagamento'),
  vehiclePlate: z.string().max(10).optional(),
})

type FormData = z.infer<typeof schema>

type Props = {
  shiftId: string
  nozzleId: string
  nozzleNumber: number
  fuelName: string
  onClose: () => void
}

export function FuelingForm({ shiftId, nozzleId, nozzleNumber, fuelName, onClose }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()

  const { data: prices = [] } = useQuery({
    queryKey: ['prices', station?.id],
    queryFn: () => getCurrentPrices(station!.id),
    enabled: !!station,
  })

  const currentPrice = prices.find((p) =>
    // FuelPrice não tem nozzleId — mas temos fuelName para correlacionar
    // Alternativa: buscar fuelId via nozzle query (já feita no NozzleList)
    // Por simplicidade, recebemos fuelName como prop e filtramos por ele
    p.fuelName === fuelName,
  )?.price ?? 0

  const {
    register,
    handleSubmit,
    watch,
    control,
    reset,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema) as any,
    defaultValues: { paymentMethod: '' },
  })

  const liters = watch('liters')
  const totalAmount = liters > 0 && currentPrice > 0 ? liters * currentPrice : 0

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      registerFueling(shiftId, {
        nozzleId,
        liters: data.liters,
        totalAmount,
        paymentMethod: data.paymentMethod,
        vehiclePlate: data.vehiclePlate || undefined,
      }),
    onSuccess: () => {
      toast.success('Abastecimento registrado!')
      qc.invalidateQueries({ queryKey: ['shift-summary', shiftId] })
      reset()
      onClose()
    },
    onError: () => {
      toast.error('Erro ao registrar abastecimento')
    },
  })

  return (
    <div className="border-t border-orange-200 bg-orange-50/50 px-4 py-3">
      <div className="mb-2 flex items-center justify-between">
        <span className="text-xs font-semibold text-orange-600">
          Bico {nozzleNumber} · {fuelName}
          {currentPrice > 0 && (
            <span className="ml-1 font-normal text-orange-400">
              · R$ {currentPrice.toFixed(3)}/L
            </span>
          )}
        </span>
        <button onClick={onClose} className="text-slate-400 dark:text-slate-500 hover:text-slate-600 dark:hover:text-slate-300">
          <X size={14} />
        </button>
      </div>

      <form
        onSubmit={handleSubmit((d) => mutation.mutate(d))}
        className="flex flex-wrap items-end gap-3"
      >
        {/* Litros */}
        <div className="min-w-[90px]">
          <Label className="text-[10px]">LITROS</Label>
          <Input
            type="number"
            step="0.001"
            min="0.001"
            placeholder="0.000"
            className="font-semibold"
            {...register('liters')}
          />
          {errors.liters && (
            <p className="mt-0.5 text-[10px] text-red-500">{errors.liters.message}</p>
          )}
        </div>

        {/* Total calculado */}
        <div className="min-w-[90px]">
          <Label className="text-[10px]">TOTAL (calc.)</Label>
          <div
            className={cn(
              'flex h-9 items-center rounded-md border px-3 text-sm font-semibold',
              totalAmount > 0
                ? 'border-green-300 bg-green-50 text-green-700'
                : 'border-slate-200 bg-slate-50 dark:bg-slate-800 text-slate-400 dark:text-slate-500',
            )}
          >
            {totalAmount > 0 ? formatBRL(totalAmount) : '—'}
          </div>
        </div>

        {/* Pagamento */}
        <div>
          <Label className="text-[10px]">PAGAMENTO</Label>
          <Controller
            control={control}
            name="paymentMethod"
            render={({ field }) => (
              <div className="flex flex-wrap gap-1">
                {PAYMENT_METHODS.map(([value, label]) => (
                  <button
                    key={value}
                    type="button"
                    onClick={() => field.onChange(value)}
                    className={cn(
                      'rounded border px-2 py-1 text-[10px] font-semibold',
                      field.value === value
                        ? 'border-orange-600 bg-orange-50 text-orange-600'
                        : 'border-slate-200 bg-white dark:bg-slate-900 text-slate-500 dark:text-slate-400 hover:border-slate-300',
                    )}
                  >
                    {label}
                  </button>
                ))}
              </div>
            )}
          />
          {errors.paymentMethod && (
            <p className="mt-0.5 text-[10px] text-red-500">{errors.paymentMethod.message}</p>
          )}
        </div>

        {/* Placa (opcional) */}
        <div className="min-w-[80px]">
          <Label className="text-[10px]">PLACA (opc.)</Label>
          <Input
            placeholder="ABC-1234"
            className="uppercase"
            maxLength={10}
            {...register('vehiclePlate')}
          />
        </div>

        <Button
          type="submit"
          disabled={mutation.isPending}
          className="bg-green-600 hover:bg-green-700"
        >
          {mutation.isPending ? '…' : '✓ Confirmar'}
        </Button>
      </form>
    </div>
  )
}
