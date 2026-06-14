import { useEffect } from 'react'
import { useForm, Controller } from 'react-hook-form'
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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { createFuel, updateFuel } from '@/api/fuels'
import type { Fuel } from '@/types'

const schema = z.object({
  name: z.string().min(1, 'Obrigatório').max(50),
  unit: z.string().min(1, 'Selecione uma unidade'),
})

type FormData = z.infer<typeof schema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  fuel?: Fuel
}

export function FuelSheet({ open, onOpenChange, fuel }: Props) {
  const qc = useQueryClient()
  const isEdit = !!fuel

  const {
    register,
    handleSubmit,
    control,
    reset,
    formState: { errors },
  } = useForm<FormData>({ resolver: zodResolver(schema) as any })

  useEffect(() => {
    if (open) {
      reset(
        fuel
          ? { name: fuel.name, unit: fuel.unit }
          : { name: '', unit: 'LITER' },
      )
    }
  }, [open, fuel, reset])

  const mutation = useMutation({
    mutationFn: (data: FormData) =>
      isEdit ? updateFuel(fuel.id, data) : createFuel(data),
    onSuccess: () => {
      toast.success(isEdit ? 'Combustível atualizado!' : 'Combustível criado!')
      qc.invalidateQueries({ queryKey: ['fuels'] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao salvar combustível')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar combustível' : 'Novo combustível'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Nome</Label>
            <Input placeholder="Ex: Gasolina Comum" {...register('name')} />
            {errors.name && <p className="mt-1 text-xs text-red-500">{errors.name.message}</p>}
          </div>

          <div>
            <Label>Unidade</Label>
            <Controller
              control={control}
              name="unit"
              render={({ field }) => (
                <Select onValueChange={field.onChange} value={field.value}>
                  <SelectTrigger>
                    <SelectValue placeholder="Selecionar unidade…" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="LITER">Litro</SelectItem>
                  </SelectContent>
                </Select>
              )}
            />
            {errors.unit && (
              <p className="mt-1 text-xs text-red-500">{errors.unit.message as string}</p>
            )}
          </div>

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando…' : isEdit ? 'Salvar alterações' : 'Criar combustível'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
