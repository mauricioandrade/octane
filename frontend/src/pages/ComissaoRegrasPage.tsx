import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { PlusCircle, Pencil, ToggleLeft, ToggleRight } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { TopBar } from '@/components/layout/TopBar'
import { CommissionRuleSheet } from '@/components/commission/CommissionRuleSheet'
import { useActiveStation } from '@/hooks/useActiveStation'
import { listCommissionRules, toggleCommissionRuleStatus } from '@/api/commissions'
import type { CommissionRule } from '@/types'

type ActiveFilter = 'all' | 'active' | 'inactive'

function formatDate(iso: string): string {
  return new Intl.DateTimeFormat('pt-BR', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  }).format(new Date(iso))
}

export function ComissaoRegrasPage() {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const [sheetOpen, setSheetOpen] = useState(false)
  const [editingRule, setEditingRule] = useState<CommissionRule | undefined>(undefined)
  const [activeFilter, setActiveFilter] = useState<ActiveFilter>('all')

  const activeParam =
    activeFilter === 'active' ? true : activeFilter === 'inactive' ? false : undefined

  const { data: rules = [], isLoading } = useQuery({
    queryKey: ['commission-rules', station?.id, activeFilter],
    queryFn: () => listCommissionRules(station!.id, activeParam),
    enabled: !!station,
  })

  const toggleMutation = useMutation({
    mutationFn: ({ id, active }: { id: string; active: boolean }) =>
      toggleCommissionRuleStatus(id, active),
    onSuccess: (_, { active }) => {
      toast.success(active ? 'Regra ativada!' : 'Regra desativada!')
      qc.invalidateQueries({ queryKey: ['commission-rules', station?.id] })
    },
    onError: () => {
      toast.error('Erro ao alterar status da regra')
    },
  })

  function handleEdit(rule: CommissionRule) {
    setEditingRule(rule)
    setSheetOpen(true)
  }

  function handleNewRule() {
    setEditingRule(undefined)
    setSheetOpen(true)
  }

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center p-6 text-sm text-slate-400">
        Selecione um posto para ver as regras de comissão.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Regras de Comissão" />

      <div className="flex flex-1 flex-col gap-4 overflow-auto p-6">
        {/* Toolbar */}
        <div className="flex flex-wrap items-center justify-between gap-3">
          <Select
            value={activeFilter}
            onValueChange={(v) => setActiveFilter(v as ActiveFilter)}
          >
            <SelectTrigger className="h-8 w-36 text-xs">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="all">Todas</SelectItem>
              <SelectItem value="active">Ativas</SelectItem>
              <SelectItem value="inactive">Inativas</SelectItem>
            </SelectContent>
          </Select>

          <Button
            size="sm"
            onClick={handleNewRule}
            className="bg-orange-600 hover:bg-orange-700 text-white"
          >
            <PlusCircle size={14} className="mr-1" />
            Nova regra
          </Button>
        </div>

        {/* Table */}
        {isLoading ? (
          <div className="flex flex-col gap-2">
            {Array.from({ length: 4 }).map((_, i) => (
              <Skeleton key={i} className="h-10 w-full rounded" />
            ))}
          </div>
        ) : rules.length === 0 ? (
          <div className="flex flex-1 items-center justify-center py-16 text-sm text-slate-400">
            Nenhuma regra encontrada.
          </div>
        ) : (
          <div className="rounded-lg border bg-white dark:bg-slate-900 overflow-hidden">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50 dark:bg-slate-800 text-xs text-slate-500 dark:text-slate-400">
                  <th className="px-4 py-3 text-left font-medium">Funcionário</th>
                  <th className="px-4 py-3 text-left font-medium">Taxa %</th>
                  <th className="px-4 py-3 text-left font-medium">Status</th>
                  <th className="px-4 py-3 text-left font-medium">Criado em</th>
                  <th className="px-4 py-3 text-right font-medium">Ações</th>
                </tr>
              </thead>
              <tbody>
                {rules.map((rule) => (
                  <tr
                    key={rule.id}
                    className="border-b last:border-0 hover:bg-slate-50 dark:hover:bg-slate-800/50"
                  >
                    <td className="px-4 py-3 font-medium text-slate-800 dark:text-slate-100">
                      {rule.employeeName}
                    </td>
                    <td className="px-4 py-3 text-slate-600 dark:text-slate-300">
                      {(rule.rate * 100).toFixed(2)}%
                    </td>
                    <td className="px-4 py-3">
                      <Badge
                        variant={rule.active ? 'default' : 'secondary'}
                        className={
                          rule.active
                            ? 'bg-green-100 text-green-700 dark:bg-green-900 dark:text-green-300'
                            : 'bg-slate-100 text-slate-500 dark:bg-slate-700 dark:text-slate-400'
                        }
                      >
                        {rule.active ? 'Ativa' : 'Inativa'}
                      </Badge>
                    </td>
                    <td className="px-4 py-3 text-xs text-slate-400 dark:text-slate-500">
                      {formatDate(rule.createdAt)}
                    </td>
                    <td className="px-4 py-3">
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          size="sm"
                          variant="ghost"
                          className="h-7 px-2 text-xs"
                          onClick={() => handleEdit(rule)}
                        >
                          <Pencil size={13} className="mr-1" />
                          Editar
                        </Button>
                        <Button
                          size="sm"
                          variant="ghost"
                          className="h-7 px-2 text-xs"
                          disabled={toggleMutation.isPending}
                          onClick={() =>
                            toggleMutation.mutate({ id: rule.id, active: !rule.active })
                          }
                        >
                          {rule.active ? (
                            <ToggleRight size={13} className="mr-1 text-green-600" />
                          ) : (
                            <ToggleLeft size={13} className="mr-1 text-slate-400" />
                          )}
                          {rule.active ? 'Desativar' : 'Ativar'}
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <CommissionRuleSheet
        open={sheetOpen}
        onOpenChange={(open) => {
          setSheetOpen(open)
          if (!open) setEditingRule(undefined)
        }}
        stationId={station.id}
        rule={editingRule}
      />
    </div>
  )
}
