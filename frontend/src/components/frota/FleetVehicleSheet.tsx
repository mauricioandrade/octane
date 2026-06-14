import { useEffect } from 'react'
import { useForm, Controller } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { createFleetVehicle, updateFleetVehicle } from '@/api/fleet-vehicles'
import { getFuels } from '@/api/fuels'
import type { FleetVehicle } from '@/types'

const schema = z.object({
  plate: z
    .string()
    .min(1, 'Obrigatório')
    .max(10)
    .toUpperCase(),
  model: z.string().max(100).optional(),
  allowedFuelId: z.string().min(1, 'Selecione um combustível'),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  vehicle?: FleetVehicle
  clientId: string
}

export function FleetVehicleSheet({ open, onOpenChange, vehicle, clientId }: Props) {
  const qc = useQueryClient()
  const isEdit = !!vehicle

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(true),
  })

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (open) {
      reset(
        vehicle
          ? { plate: vehicle.plate, model: vehicle.model ?? '', allowedFuelId: vehicle.allowedFuelId }
          : { plate: '', model: '', allowedFuelId: '' },
      )
    }
  }, [open, vehicle, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) => {
      const model = data.model?.trim() || undefined
      if (isEdit) {
        return updateFleetVehicle(vehicle.id, {
          model,
          allowedFuelId: data.allowedFuelId,
          active: vehicle.active,
        })
      }
      return createFleetVehicle({
        clientId,
        plate: data.plate,
        model,
        allowedFuelId: data.allowedFuelId,
      })
    },
    onSuccess: () => {
      toast.success(isEdit ? 'Veículo atualizado!' : 'Veículo criado!')
      qc.invalidateQueries({ queryKey: ['fleet-vehicles', clientId] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao salvar veículo')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar veículo' : 'Novo veículo'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Placa</Label>
            <Input
              placeholder="Ex: ABC-1234"
              className="uppercase"
              disabled={isEdit}
              {...register('plate')}
            />
            {errors.plate && <p className="mt-1 text-xs text-red-500">{errors.plate.message}</p>}
          </div>

          <div>
            <Label>Modelo (opcional)</Label>
            <Input placeholder="Ex: Volkswagen Gol" {...register('model')} />
            {errors.model && <p className="mt-1 text-xs text-red-500">{errors.model.message}</p>}
          </div>

          <div>
            <Label>Combustível Permitido</Label>
            <Controller
              control={control}
              name="allowedFuelId"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Selecionar combustível…" />
                  </SelectTrigger>
                  <SelectContent>
                    {fuels.map((fuel) => (
                      <SelectItem key={fuel.id} value={fuel.id}>
                        {fuel.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.allowedFuelId && (
              <p className="mt-1 text-xs text-red-500">{errors.allowedFuelId.message}</p>
            )}
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar alterações' : 'Criar veículo'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
