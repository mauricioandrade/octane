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
import { createCommissionRule, updateCommissionRule } from '@/api/commissions'
import type { CommissionRule } from '@/types'

const createSchema = z.object({
  employeeName: z.string().min(1, 'Nome obrigatório').max(100, 'Máximo 100 caracteres'),
  ratePercent: z
    .number({ error: 'Taxa obrigatória' })
    .min(0.01, 'Taxa mínima de 0,01%')
    .max(100, 'Taxa máxima de 100%'),
})

const editSchema = z.object({
  ratePercent: z
    .number({ error: 'Taxa obrigatória' })
    .min(0.01, 'Taxa mínima de 0,01%')
    .max(100, 'Taxa máxima de 100%'),
})

type CreateFormData = z.infer<typeof createSchema>
type EditFormData = z.infer<typeof editSchema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  stationId: string
  rule?: CommissionRule
}

export function CommissionRuleSheet({ open, onOpenChange, stationId, rule }: Props) {
  const qc = useQueryClient()
  const isEdit = !!rule

  const createForm = useForm<CreateFormData>({
    resolver: zodResolver(createSchema),
    defaultValues: { employeeName: '', ratePercent: undefined },
  })

  const editForm = useForm<EditFormData>({
    resolver: zodResolver(editSchema),
    defaultValues: { ratePercent: undefined },
  })

  useEffect(() => {
    if (open) {
      if (isEdit && rule) {
        editForm.reset({ ratePercent: +(rule.rate * 100).toFixed(4) })
      } else {
        createForm.reset({ employeeName: '', ratePercent: undefined })
      }
    }
  }, [open, isEdit, rule]) // eslint-disable-line react-hooks/exhaustive-deps

  const createMutation = useMutation({
    mutationFn: (data: CreateFormData) =>
      createCommissionRule({
        stationId,
        employeeName: data.employeeName.trim(),
        rate: data.ratePercent / 100,
      }),
    onSuccess: () => {
      toast.success('Regra criada com sucesso!')
      qc.invalidateQueries({ queryKey: ['commission-rules', stationId] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao criar regra de comissão')
    },
  })

  const editMutation = useMutation({
    mutationFn: (data: EditFormData) =>
      updateCommissionRule(rule!.id, { rate: data.ratePercent / 100 }),
    onSuccess: () => {
      toast.success('Regra atualizada!')
      qc.invalidateQueries({ queryKey: ['commission-rules', stationId] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao atualizar regra de comissão')
    },
  })

  const isPending = createMutation.isPending || editMutation.isPending

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar regra de comissão' : 'Nova regra de comissão'}</SheetTitle>
        </SheetHeader>

        {isEdit ? (
          <form
            onSubmit={editForm.handleSubmit((d) => editMutation.mutate(d))}
            className="mt-4 flex flex-col gap-4"
          >
            <div>
              <Label className="text-xs text-slate-500">Funcionário</Label>
              <p className="mt-1 text-sm font-medium text-slate-800 dark:text-slate-200">
                {rule?.employeeName}
              </p>
            </div>

            <div>
              <Label htmlFor="edit-rate">Taxa de comissão (%)</Label>
              <Input
                id="edit-rate"
                type="number"
                step="0.01"
                min="0.01"
                max="100"
                placeholder="Ex: 2.00"
                {...editForm.register('ratePercent', { valueAsNumber: true })}
              />
              {editForm.formState.errors.ratePercent && (
                <p className="mt-1 text-xs text-red-500">
                  {editForm.formState.errors.ratePercent.message}
                </p>
              )}
            </div>

            <Button
              type="submit"
              disabled={isPending}
              className="mt-2 bg-orange-600 hover:bg-orange-700"
            >
              {isPending ? 'Salvando…' : 'Salvar'}
            </Button>
          </form>
        ) : (
          <form
            onSubmit={createForm.handleSubmit((d) => createMutation.mutate(d))}
            className="mt-4 flex flex-col gap-4"
          >
            <div>
              <Label htmlFor="create-name">Nome do funcionário</Label>
              <Input
                id="create-name"
                placeholder="Ex: João Silva"
                {...createForm.register('employeeName')}
              />
              {createForm.formState.errors.employeeName && (
                <p className="mt-1 text-xs text-red-500">
                  {createForm.formState.errors.employeeName.message}
                </p>
              )}
            </div>

            <div>
              <Label htmlFor="create-rate">Taxa de comissão (%)</Label>
              <Input
                id="create-rate"
                type="number"
                step="0.01"
                min="0.01"
                max="100"
                placeholder="Ex: 2.00"
                {...createForm.register('ratePercent', { valueAsNumber: true })}
              />
              {createForm.formState.errors.ratePercent && (
                <p className="mt-1 text-xs text-red-500">
                  {createForm.formState.errors.ratePercent.message}
                </p>
              )}
            </div>

            <Button
              type="submit"
              disabled={isPending}
              className="mt-2 bg-orange-600 hover:bg-orange-700"
            >
              {isPending ? 'Criando…' : 'Criar regra'}
            </Button>
          </form>
        )}
      </SheetContent>
    </Sheet>
  )
}
