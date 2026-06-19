import { useEffect } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { z } from 'zod'
import { useMutation, useQueryClient } from '@tanstack/react-query'
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
import { createServiceOrder } from '@/api/service-orders'
import { useActiveStation } from '@/hooks/useActiveStation'

const schema = z.object({
  plate: z.string().min(1, 'Placa obrigatória').max(10),
  odometer: z
    .number({ error: 'Hodômetro obrigatório' })
    .positive('Hodômetro deve ser maior que zero'),
  customerName: z.string().max(100).optional(),
  customerPhone: z.string().max(20).optional(),
  notes: z.string().max(500).optional(),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
}

export function ServiceOrderSheet({ open, onOpenChange }: Props) {
  const qc = useQueryClient()
  const { station } = useActiveStation()

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { odometer: undefined },
  })

  useEffect(() => {
    if (open) {
      reset({ plate: '', odometer: undefined, customerName: '', customerPhone: '', notes: '' })
    }
  }, [open, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) => {
      return createServiceOrder({
        stationId: station!.id,
        plate: data.plate.toUpperCase().trim(),
        odometer: data.odometer,
        customerName: data.customerName?.trim() || undefined,
        customerPhone: data.customerPhone?.trim() || undefined,
        notes: data.notes?.trim() || undefined,
      })
    },
    onSuccess: () => {
      toast.success('Ordem de serviço criada!')
      qc.invalidateQueries({ queryKey: ['service-orders'] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao criar ordem de serviço')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>Nova Ordem de Serviço</SheetTitle>
        </SheetHeader>

        <form
          onSubmit={handleSubmit((d) => mutation.mutate(d))}
          className="mt-4 flex flex-col gap-4"
        >
          <div>
            <Label>Placa</Label>
            <Input
              placeholder="Ex: ABC1234"
              {...register('plate')}
              onChange={(e) => setValue('plate', e.target.value.toUpperCase())}
            />
            {errors.plate && (
              <p className="mt-1 text-xs text-red-500">{errors.plate.message}</p>
            )}
          </div>

          <div>
            <Label>Hodômetro (km)</Label>
            <Input
              type="number"
              min="1"
              placeholder="Ex: 45000"
              {...register('odometer', { valueAsNumber: true })}
            />
            {errors.odometer && (
              <p className="mt-1 text-xs text-red-500">{errors.odometer.message}</p>
            )}
          </div>

          <div>
            <Label>Nome do cliente (opcional)</Label>
            <Input placeholder="Ex: João Silva" {...register('customerName')} />
            {errors.customerName && (
              <p className="mt-1 text-xs text-red-500">{errors.customerName.message}</p>
            )}
          </div>

          <div>
            <Label>Telefone (opcional)</Label>
            <Input placeholder="Ex: (11) 99999-9999" {...register('customerPhone')} />
            {errors.customerPhone && (
              <p className="mt-1 text-xs text-red-500">{errors.customerPhone.message}</p>
            )}
          </div>

          <div>
            <Label>Observações (opcional)</Label>
            <Input placeholder="Ex: Troca de óleo e filtro" {...register('notes')} />
            {errors.notes && (
              <p className="mt-1 text-xs text-red-500">{errors.notes.message}</p>
            )}
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending || !station}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Criando…' : 'Criar OS'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
