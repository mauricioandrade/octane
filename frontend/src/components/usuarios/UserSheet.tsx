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
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { createUser, updateUser } from '@/api/users'
import type { AppUser, UserRole } from '@/types'
import { USER_ROLE_LABELS } from '@/types'

const createSchema = z.object({
  name: z.string().min(1, 'Obrigatório'),
  username: z
    .string()
    .min(3, 'Mínimo 3 caracteres')
    .max(50)
    .regex(/^[a-zA-Z0-9_]+$/, 'Apenas letras, números e _'),
  password: z.string().min(6, 'Mínimo 6 caracteres'),
  role: z.enum(['ADMIN', 'MANAGER', 'ATTENDANT']),
})

const editSchema = z.object({
  name: z.string().min(1, 'Obrigatório'),
  username: z.string(),
  password: z.string().optional().refine(
    (v) => !v || v.length === 0 || v.length >= 6,
    { message: 'Mínimo 6 caracteres' },
  ),
  role: z.enum(['ADMIN', 'MANAGER', 'ATTENDANT']),
  active: z.boolean(),
})

type CreateFormData = z.infer<typeof createSchema>
type EditFormData = z.infer<typeof editSchema>

type Props = {
  open: boolean
  onOpenChange: (open: boolean) => void
  user?: AppUser
}

export function UserSheet({ open, onOpenChange, user }: Props) {
  const qc = useQueryClient()
  const isEdit = !!user

  const {
    register,
    handleSubmit,
    reset,
    setValue,
    watch,
    formState: { errors },
  } = useForm<CreateFormData | EditFormData>({
    resolver: zodResolver(isEdit ? editSchema : createSchema),
  })

  const currentRole = watch('role')
  const currentActive = isEdit ? watch('active' as keyof EditFormData) : undefined

  useEffect(() => {
    if (open) {
      if (user) {
        reset({
          name: user.name,
          username: user.username,
          password: '',
          role: user.role,
          active: user.active,
        } as EditFormData)
      } else {
        reset({
          name: '',
          username: '',
          password: '',
          role: 'ATTENDANT',
        } as CreateFormData)
      }
    }
  }, [open, user, reset])

  const mutation = useMutation({
    mutationFn: (data: CreateFormData | EditFormData) => {
      if (isEdit) {
        const editData = data as EditFormData
        return updateUser(user.id, {
          name: editData.name,
          role: editData.role as UserRole,
          active: editData.active,
          password: editData.password && editData.password.length > 0 ? editData.password : undefined,
        })
      }
      const createData = data as CreateFormData
      return createUser({
        username: createData.username,
        password: createData.password,
        name: createData.name,
        role: createData.role as UserRole,
      })
    },
    onSuccess: () => {
      toast.success(isEdit ? 'Usuário atualizado!' : 'Usuário criado!')
      qc.invalidateQueries({ queryKey: ['users'] })
      onOpenChange(false)
    },
    onError: () => {
      toast.error('Erro ao salvar usuário')
    },
  })

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent className="w-[380px] overflow-y-auto">
        <SheetHeader>
          <SheetTitle>{isEdit ? 'Editar usuário' : 'Novo usuário'}</SheetTitle>
        </SheetHeader>

        <form onSubmit={handleSubmit((d) => mutation.mutate(d))} className="mt-4 flex flex-col gap-4">
          <div>
            <Label>Nome</Label>
            <Input placeholder="Ex: João Silva" {...register('name')} />
            {errors.name && (
              <p className="mt-1 text-xs text-red-500">{errors.name.message}</p>
            )}
          </div>

          <div>
            <Label>Usuário</Label>
            <Input
              placeholder="Ex: joao_silva"
              disabled={isEdit}
              {...register('username')}
            />
            {errors.username && (
              <p className="mt-1 text-xs text-red-500">{errors.username.message}</p>
            )}
          </div>

          <div>
            <Label>{isEdit ? 'Nova senha (deixe vazio para manter)' : 'Senha'}</Label>
            <Input
              type="password"
              placeholder={isEdit ? 'Deixe vazio para manter' : 'Mínimo 6 caracteres'}
              {...register('password')}
            />
            {errors.password && (
              <p className="mt-1 text-xs text-red-500">{errors.password.message as string}</p>
            )}
          </div>

          <div>
            <Label>Perfil</Label>
            <Select
              value={currentRole}
              onValueChange={(value) => setValue('role', value as UserRole, { shouldValidate: true })}
            >
              <SelectTrigger>
                <SelectValue placeholder="Selecione o perfil" />
              </SelectTrigger>
              <SelectContent>
                {(Object.entries(USER_ROLE_LABELS) as [UserRole, string][]).map(([value, label]) => (
                  <SelectItem key={value} value={value}>
                    {label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.role && (
              <p className="mt-1 text-xs text-red-500">{errors.role.message}</p>
            )}
          </div>

          {isEdit && (
            <div className="flex items-center gap-2">
              <input
                type="checkbox"
                id="user-active"
                checked={currentActive as boolean}
                onChange={(e) => setValue('active' as keyof EditFormData, e.target.checked, { shouldValidate: true })}
                className="h-4 w-4 rounded border-slate-300"
              />
              <Label htmlFor="user-active" className="cursor-pointer">
                Ativo
              </Label>
            </div>
          )}

          <Button
            type="submit"
            disabled={mutation.isPending}
            className="mt-2 bg-orange-600 hover:bg-orange-700"
          >
            {mutation.isPending ? 'Salvando...' : isEdit ? 'Salvar alterações' : 'Criar usuário'}
          </Button>
        </form>
      </SheetContent>
    </Sheet>
  )
}
