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
import { createFleetClient, updateFleetClient } from '@/api/fleet-clients'
import type { FleetClient } from '@/types'

const schema = z.object({
  companyName: z.string().min(1, 'Obrigatório').max(150),
  cnpj: z
    .string()
    .min(1, 'Obrigatório')
    .regex(/^\d{2}\.\d{3}\.\d{3}\/\d{4}-\d{2}$/, 'Formato: XX.XXX.XXX/XXXX-XX'),
  tradeName: z.string().max(150).optional(),
  monthlyLimit: z
    .string()
    .optional()
    .refine(
      (v) => !v || v.trim() === '' || (!isNaN(parseFloat(v)) && parseFloat(v) > 0),
      { message: 'Deve ser um valor positivo' },
    ),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  client?: FleetClient
  stationId: string
}

export function FleetClientSheet({ open, onOpenChange, client, stationId }: Props) {
  const qc = useQueryClient()
  const isEdit = !!client

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (open) {
      reset(
        client
          ? {
              companyName: client.companyName,
              cnpj: client.cnpj,
              tradeName: client.tradeName ?? '',
              monthlyLimit: client.monthlyLimit !== undefined ? String(client.monthlyLimit) : '',
            }
          : { companyName: '', cnpj: '', tradeName: '', monthlyLimit: '' },
      )
    }
  }, [open, client, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) => {
      const monthlyLimit =
        data.monthlyLimit && data.monthlyLimit.trim() !== ''
          ? parseFloat(data.monthlyLimit)
          : undefined
      const tradeName = data.tradeName?.trim() || undefined

      if (isEdit) {
        return updateFleetClient(client.id, {
          companyName: data.companyName,
          tradeName,
          monthlyLimit,
          active: client.active,
        })
      }
      return createFleetClient({
        stationId,
        cnpj: data.cnpj,
        companyName: data.companyName,
        tradeName,
        monthlyLimit,
      })
    },
    onSuccess: () => {
      toast.success(isEdit ? 'Cliente atualizado!' : 'Cliente criado!')
      qc.invalidateQueries({ queryKey: ['fleet-clients'] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao salvar cliente')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar cliente' : 'Novo cliente de frota'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Razão Social</Label>
            <Input placeholder="Ex: Transportes Silva Ltda" {...register('companyName')} />
            {errors.companyName && (
              <p className="mt-1 text-xs text-red-500">{errors.companyName.message}</p>
            )}
          </div>

          <div>
            <Label>CNPJ</Label>
            <Input
              placeholder="XX.XXX.XXX/XXXX-XX"
              disabled={isEdit}
              {...register('cnpj')}
            />
            {errors.cnpj && <p className="mt-1 text-xs text-red-500">{errors.cnpj.message}</p>}
          </div>

          <div>
            <Label>Nome Fantasia (opcional)</Label>
            <Input placeholder="Ex: Trans Silva" {...register('tradeName')} />
            {errors.tradeName && (
              <p className="mt-1 text-xs text-red-500">{errors.tradeName.message}</p>
            )}
          </div>

          <div>
            <Label>Limite Mensal em R$ (opcional)</Label>
            <Input
              type="number"
              step="0.01"
              min="0"
              placeholder="Ex: 5000.00"
              {...register('monthlyLimit')}
            />
            {errors.monthlyLimit && (
              <p className="mt-1 text-xs text-red-500">{errors.monthlyLimit.message as string}</p>
            )}
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar alterações' : 'Criar cliente'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
