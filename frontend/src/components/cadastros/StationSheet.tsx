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
import { createStation, updateStation } from '@/api/stations'
import type { Station } from '@/types'

const schema = z.object({
  name: z.string().min(1, 'Obrigatório').max(100),
  cnpj: z.string().min(1, 'Obrigatório').max(20),
  address: z.string().min(1, 'Obrigatório').max(200),
  city: z.string().min(1, 'Obrigatório').max(100),
  state: z.string().length(2, 'UF deve ter 2 caracteres'),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  station?: Station
}

export function StationSheet({ open, onOpenChange, station }: Props) {
  const qc = useQueryClient()
  const isEdit = !!station

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) })

  useEffect(() => {
    if (open) {
      reset(
        station
          ? { name: station.name, cnpj: station.cnpj, address: station.address, city: station.city, state: station.state }
          : { name: '', cnpj: '', address: '', city: '', state: '' },
      )
    }
  }, [open, station, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit ? updateStation(station.id, data) : createStation(data),
    onSuccess: () => {
      toast.success(isEdit ? 'Posto atualizado!' : 'Posto criado!')
      qc.invalidateQueries({ queryKey: ['stations'] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error(isEdit ? 'Erro ao atualizar posto' : 'Erro ao criar posto')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar posto' : 'Novo posto'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Nome</Label>
            <Input placeholder="Ex: Posto Central" {...register('name')} />
            {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name.message}</p>}
          </div>

          <div>
            <Label>CNPJ</Label>
            <Input placeholder="00.000.000/0000-00" {...register('cnpj')} />
            {errors.cnpj && <p className="mt-1 text-xs text-red-500">{errors.cnpj.message}</p>}
          </div>

          <div>
            <Label>Endereço</Label>
            <Input placeholder="Rua, número" {...register('address')} />
            {errors.address && <p className="mt-1 text-xs text-red-500">{errors.address.message}</p>}
          </div>

          <div className="grid grid-cols-[1fr_60px] gap-3">
            <div>
              <Label>Cidade</Label>
              <Input placeholder="São Paulo" {...register('city')} />
              {errors.city && <p className="mt-1 text-xs text-red-500">{errors.city.message}</p>}
            </div>
            <div>
              <Label>UF</Label>
              <Input
                placeholder="SP"
                maxLength={2}
                className="uppercase"
                {...register('state')}
              />
              {errors.state && <p className="mt-1 text-xs text-red-500">{errors.state.message}</p>}
            </div>
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar alterações' : 'Criar posto'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
