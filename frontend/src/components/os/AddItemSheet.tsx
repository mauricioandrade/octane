import { useEffect } from 'react'
import { useForm, useWatch } from 'react-hook-form'
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
import { addServiceOrderItem } from '@/api/service-orders'
import { formatBRL } from '@/lib/utils'

const schema = z.object({
  description: z.string().min(1, 'Descrição obrigatória').max(200),
  quantity: z
    .number({ error: 'Quantidade obrigatória' })
    .positive('Deve ser maior que zero'),
  unitPrice: z
    .number({ error: 'Preço obrigatório' })
    .positive('Deve ser maior que zero'),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  serviceOrderId: string
}

export function AddItemSheet({ open, onOpenChange, serviceOrderId }: Props) {
  const qc = useQueryClient()

  const {
    register,
    handleSubmit,
    reset,
    control,
    formState: { errors },
  } = useForm<FormData>({
    resolver: zodResolver(schema),
    defaultValues: { quantity: 1, unitPrice: undefined },
  })

  const quantity = useWatch({ control, name: 'quantity' })
  const unitPrice = useWatch({ control, name: 'unitPrice' })
  const preview = (quantity || 0) * (unitPrice || 0)

  useEffect(() => {
    if (open) {
      reset({ description: '', quantity: 1, unitPrice: undefined })
    }
  }, [open, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      addServiceOrderItem(serviceOrderId, {
        description: data.description.trim(),
        quantity: data.quantity,
        unitPrice: data.unitPrice,
      }),
    onSuccess: () => {
      toast.success('Item adicionado!')
      qc.invalidateQueries({ queryKey: ['service-order', serviceOrderId] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao adicionar item')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>Adicionar Item</SheetTitle>
        </SheetHeader>

        <form
          onSubmit={handleSubmit((d) => mutation.mutate(d))}
          className="mt-4 flex flex-col gap-4"
        >
          <div>
            <Label>Descrição</Label>
            <Input placeholder="Ex: Troca de óleo" {...register('description')} />
            {errors.description && (
              <p className="mt-1 text-xs text-red-500">{errors.description.message}</p>
            )}
          </div>

          <div>
            <Label>Quantidade</Label>
            <Input
              type="number"
              min="0.01"
              step="0.01"
              {...register('quantity', { valueAsNumber: true })}
            />
            {errors.quantity && (
              <p className="mt-1 text-xs text-red-500">{errors.quantity.message}</p>
            )}
          </div>

          <div>
            <Label>Preço unitário (R$)</Label>
            <Input
              type="number"
              min="0.01"
              step="0.01"
              placeholder="Ex: 45.00"
              {...register('unitPrice', { valueAsNumber: true })}
            />
            {errors.unitPrice && (
              <p className="mt-1 text-xs text-red-500">{errors.unitPrice.message}</p>
            )}
          </div>

          {preview > 0 && (
            <div className="rounded-md bg-slate-50 dark:bg-slate-800 px-3 py-2 text-sm">
              <span className="text-slate-500 dark:text-slate-400">Total do item: </span>
              <span className="font-semibold text-slate-800 dark:text-slate-100">
                {formatBRL(preview)}
              </span>
            </div>
          )}

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Adicionando…' : 'Adicionar item'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
