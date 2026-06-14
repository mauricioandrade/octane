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
import { createFleetDriver, updateFleetDriver } from '@/api/fleet-drivers'
import type { FleetDriver } from '@/types'

const schema = z
  .object({
    name: z.string().min(1, 'Obrigatório').max(150),
    cpf: z
      .string()
      .min(1, 'Obrigatório')
      .regex(/^\d{3}\.\d{3}\.\d{3}-\d{2}$/, 'Formato: XXX.XXX.XXX-XX'),
    pin: z
      .string()
      .optional()
      .refine((v) => !v || /^\d{6}$/.test(v), { message: 'PIN deve ter 6 dígitos' }),
    rfidTag: z.string().max(100).optional(),
  })
  .refine(
    (d) => (d.pin && d.pin.trim() !== '') || (d.rfidTag && d.rfidTag.trim() !== ''),
    { message: 'Preencha ao menos PIN ou Tag RFID', path: ['pin'] },
  )

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  driver?: FleetDriver
  clientId: string
}

export function FleetDriverSheet({ open, onOpenChange, driver, clientId }: Props) {
  const qc = useQueryClient()
  const isEdit = !!driver

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (open) {
      reset(
        driver
          ? { name: driver.name, cpf: driver.cpf, pin: '', rfidTag: '' }
          : { name: '', cpf: '', pin: '', rfidTag: '' },
      )
    }
  }, [open, driver, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) => {
      const pin = data.pin?.trim() || undefined
      const rfidTag = data.rfidTag?.trim() || undefined
      if (isEdit) {
        return updateFleetDriver(driver.id, { name: data.name, pin, rfidTag, active: driver.active })
      }
      return createFleetDriver({ clientId, name: data.name, cpf: data.cpf, pin, rfidTag })
    },
    onSuccess: () => {
      toast.success(isEdit ? 'Motorista atualizado!' : 'Motorista criado!')
      qc.invalidateQueries({ queryKey: ['fleet-drivers', clientId] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao salvar motorista')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar motorista' : 'Novo motorista'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Nome</Label>
            <Input placeholder="Ex: João da Silva" {...register('name')} />
            {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name.message}</p>}
          </div>

          <div>
            <Label>CPF</Label>
            <Input
              placeholder="XXX.XXX.XXX-XX"
              disabled={isEdit}
              {...register('cpf')}
            />
            {errors.cpf && <p className="mt-1 text-xs text-red-500">{errors.cpf.message}</p>}
          </div>

          <div>
            <Label>PIN — 6 dígitos (opcional)</Label>
            <Input
              type="password"
              placeholder="••••••"
              maxLength={6}
              {...register('pin')}
            />
            {errors.pin && <p className="mt-1 text-xs text-red-500">{errors.pin.message}</p>}
          </div>

          <div>
            <Label>Tag RFID (opcional)</Label>
            <Input placeholder="Ex: A1B2C3D4" {...register('rfidTag')} />
            {errors.rfidTag && (
              <p className="mt-1 text-xs text-red-500">{errors.rfidTag.message}</p>
            )}
          </div>

          <p className="text-xs text-slate-400">
            Ao menos PIN ou Tag RFID deve ser preenchido.
            {isEdit && ' Deixe em branco para manter os valores atuais.'}
          </p>

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar alterações' : 'Criar motorista'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
