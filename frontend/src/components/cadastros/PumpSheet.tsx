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
import { createPump, updatePump } from '@/api/pumps'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { Pump } from '@/types'

const schema = z.object({
  number: z.coerce.number({ error: 'Obrigatório' }).int().min(1),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  pump?: Pump
}

export function PumpSheet({ open, onOpenChange, pump }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const isEdit = !!pump

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) as any })

  useEffect(() => {
    if (open) reset({ number: pump?.number ?? undefined })
  }, [open, pump, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? updatePump(pump.id, { number: data.number })
        : createPump(station!.id, { number: data.number }),
    onSuccess: () => {
      toast.success(isEdit ? 'Bomba atualizada!' : 'Bomba criada!')
      qc.invalidateQueries({ queryKey: ['pumps', station?.id] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error(isEdit ? 'Erro ao atualizar bomba' : 'Erro ao criar bomba')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[320px]">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar bomba' : 'Nova bomba'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Número da bomba</Label>
            <Input type="number" min="1" placeholder="1" {...register('number')} />
            {errors.number && (
              <p className="mt-1 text-xs text-red-500">{errors.number.message as string}</p>
            )}
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending || !station}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar' : 'Criar bomba'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
