import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { formatBRL } from '@/lib/utils'
import { createPrice } from '@/api/prices'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { FuelPrice, Fuel } from '@/types'

const schema = z.object({
  fuelId: z.string().min(1, 'Selecione um combustível'),
  price: z.number({ error: 'Obrigatório' }).positive('Deve ser maior que 0'),
})

type FormData = z.infer<typeof schema>

type Props = {
  fuels: Fuel[]
  currentPrices: FuelPrice[]
}

export function NewPriceForm({ fuels, currentPrices }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()

  const {
    register,
    handleSubmit,
    watch,
    control,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  const selectedFuelId = watch('fuelId')
  const newPrice = watch('price')

  const currentPrice = currentPrices.find((p) => p.fuelId === selectedFuelId)?.price
  const variation =
    currentPrice && newPrice > 0 ? newPrice - currentPrice : null
  const variationPct =
    currentPrice && variation !== null ? (variation / currentPrice) * 100 : null

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      createPrice(station!.id, { fuelId: data.fuelId, price: data.price }),
    onSuccess: () => {
      toast.success('Preço atualizado!')
      qc.invalidateQueries({ queryKey: ['prices', station?.id] })
      reset()
    },
    onError: () => {
      toast.error('Erro ao atualizar preço')
    },
  })

  return (
    <form
      onSubmit={handleSubmit((d) => mutation.mutate(d))}
      className="flex flex-col gap-4"
    >
      <h2 className="text-sm font-bold text-slate-800">Atualizar preço</h2>

      <div>
        <Label>Combustível</Label>
        <Controller
          control={control}
          name="fuelId"
          render={({ field }) => (
            <Select onValueChange={field.onChange} value={field.value}>
              <SelectTrigger>
                <SelectValue placeholder="Selecionar…" />
              </SelectTrigger>
              <SelectContent>
                {fuels.filter((f) => f.active).map((fuel) => (
                  <SelectItem key={fuel.id} value={fuel.id}>
                    {fuel.name}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        />
        {errors.fuelId && (
          <p className="mt-1 text-xs text-red-500">{errors.fuelId.message}</p>
        )}
      </div>

      <div>
        <Label>Novo preço (R$/L)</Label>
        <Input
          type="number"
          step="0.001"
          min="0.001"
          placeholder="0.000"
          className="text-lg font-bold text-orange-600"
          {...register('price', { valueAsNumber: true })}
        />
        {errors.price && (
          <p className="mt-1 text-xs text-red-500">{errors.price.message}</p>
        )}
      </div>

      {/* Prévia de variação */}
      {currentPrice !== undefined && variation !== null && variationPct !== null && (
        <div className="rounded-md border border-amber-200 bg-amber-50 p-3">
          <p className="text-xs text-amber-700">Preço anterior: {formatBRL(currentPrice)}</p>
          <p className="text-xs font-semibold text-amber-900">
            Variação:{' '}
            {variation >= 0 ? '+' : ''}
            {formatBRL(variation)} ({variationPct >= 0 ? '+' : ''}
            {variationPct.toFixed(1)}%)
          </p>
        </div>
      )}

      <Button
        type="submit"
        disabled={mutation.isPending || !station}
        className="bg-orange-600 hover:bg-orange-700"
      >
        {mutation.isPending ? 'Salvando…' : 'Confirmar preço'}
      </Button>
    </form>
  )
}
