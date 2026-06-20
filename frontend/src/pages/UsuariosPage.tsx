import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Plus, Pencil } from 'lucide-react'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { TopBar } from '@/components/layout/TopBar'
import { UserSheet } from '@/components/usuários/UserSheet'
import { getUsers } from '@/api/users'
import { USER_ROLE_LABELS } from '@/types'
import type { AppUser } from '@/types'

export function UsuáriosPage() {
  const { data: users = [], isLoading } = useQuery({
    queryKey: ['users'],
    queryFn: getUsers,
  })

  const [sheetOpen, setSheetOpen] = useState(false)
  const [editUser, setEditUser] = useState<AppUser | undefined>()

  function openCreate() {
    setEditUser(undefined)
    setSheetOpen(true)
  }

  function openEdit(user: AppUser) {
    setEditUser(user)
    setSheetOpen(true)
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Usuários" subtitle="Gerenciamento de usuários do sistema" />

      <div className="flex-1 overflow-auto p-6">
        <div className="mb-4 flex justify-end">
          <Button size="sm" className="bg-orange-600 hover:bg-orange-700" onClick={openCreate}>
            <Plus size={14} className="mr-1" />
            Novo usuário
          </Button>
        </div>

        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : users.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum usuário cadastrado.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Nome
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Usuario
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Perfil
                  </th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Status
                  </th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                    Ações
                  </th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr
                    key={user.id}
                    className="border-b last:border-0 hover:bg-slate-50 dark:hover:bg-slate-800"
                  >
                    <td className="px-4 py-3 font-semibold text-slate-800 dark:text-slate-200">
                      {user.name}
                    </td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">{user.username}</td>
                    <td className="px-4 py-3 text-slate-500 dark:text-slate-400">
                      {USER_ROLE_LABELS[user.role]}
                    </td>
                    <td className="px-4 py-3 text-center">
                      <span
                        className={
                          user.active
                            ? 'inline-block rounded-full bg-emerald-100 px-2 py-0.5 text-xs font-medium text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400'
                            : 'inline-block rounded-full bg-slate-100 px-2 py-0.5 text-xs font-medium text-slate-500 dark:bg-slate-800 dark:text-slate-400'
                        }
                      >
                        {user.active ? 'Ativo' : 'Inativo'}
                      </span>
                    </td>
                    <td className="px-4 py-3 text-center">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-7 w-7"
                        onClick={() => openEdit(user)}
                      >
                        <Pencil size={14} />
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <UserSheet
        open={sheetOpen}
        onOpenChange={setSheetOpen}
        user={editUser}
      />
    </div>
  )
}
