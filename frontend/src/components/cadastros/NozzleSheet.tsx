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
import { createNozzle, updateNozzle } from '@/api/nozzles'
import { getPumps } from '@/api/pumps'
import { getFuels } from '@/api/fuels'
import { useActiveStation } from '@/hooks/useActiveStation'
import type { Nozzle } from '@/types'

const schema = z.object({
  number: z.coerce.number({ error: 'Obrigatório' }).int().min(1),
  pumpId: z.string().min(1, 'Selecione uma bomba'),
  fuelId: z.string().min(1, 'Selecione um combustível'),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  nozzle?: Nozzle & { pumpNumber?: number; fuelName?: string }
}

export function NozzleSheet({ open, onOpenChange, nozzle }: Props) {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const isEdit = !!nozzle

  const { data: pumps = [] } = useQuery({
    queryKey: ['pumps', station?.id],
    queryFn: () => getPumps(station!.id),
    enabled: !!station && open,
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
    enabled: open,
  })

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) as any })

  useEffect(() => {
    if (open) {
      reset({
        number: nozzle?.number ?? undefined,
        pumpId: nozzle?.pumpId ?? '',
        fuelId: nozzle?.fuelId ?? '',
      })
    }
  }, [open, nozzle, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit
        ? updateNozzle(nozzle.id, data)
        : createNozzle(data.pumpId, { number: data.number, pumpId: data.pumpId, fuelId: data.fuelId }),
    onSuccess: () => {
      toast.success(isEdit ? 'Bico atualizado!' : 'Bico criado!')
      pumps.forEach((p) => qc.invalidateQueries({ queryKey: ['nozzles', p.id] }))
      onOpenChange(false)
    },
    onError: () => {
      toast.error(isEdit ? 'Erro ao atualizar bico' : 'Erro ao criar bico')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[360px]">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar bico' : 'Novo bico'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Número do bico</Label>
            <Input type="number" min="1" placeholder="1" {...register('number')} />
            {errors.number && (
              <p className="mt-1 text-xs text-red-500">{errors.number.message as string}</p>
            )}
          </div>

          <div>
            <Label>Bomba</Label>
            <Controller
              control={control}
              name="pumpId"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Selecionar bomba…" />
                  </SelectTrigger>
                  <SelectContent>
                    {pumps.filter((p) => p.status === 'ACTIVE').map((pump) => (
                      <SelectItem key={pump.id} value={pump.id}>
                        Bomba {pump.number}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              )}
            />
            {errors.pumpId && (
              <p className="mt-1 text-xs text-red-500">{errors.pumpId.message as string}</p>
            )}
          </div>

          <div>
            <Label>Combustível</Label>
            <Controller
              control={control}
              name="fuelId"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Selecionar combustível…" />
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
              <p className="mt-1 text-xs text-red-500">{errors.fuelId.message as string}</p>
            )}
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending || !station}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar' : 'Criar bico'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
