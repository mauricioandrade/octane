import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { TopBar } from '@/components/layout/TopBar'
import { listAuditLogs } from '@/api/audit'

const ACTION_LABELS: Record<string, string> = {
  CREATE: 'Criação',
  UPDATE: 'Atualização',
  DELETE: 'Exclusão',
  LOGIN: 'Login',
  LOGOUT: 'Logout',
  OPEN: 'Abertura',
  CLOSE: 'Fechamento',
  UPDATE_STATIONS: 'Postos atualizados',
}

const ENTITY_LABELS: Record<string, string> = {
  Station: 'Posto',
  Pump: 'Bomba',
  Nozzle: 'Bico',
  Fuel: 'Combustível',
  Shift: 'Turno',
  CashRegister: 'Caixa',
  FleetClient: 'Cliente Frota',
  FleetDriver: 'Motorista Frota',
  FleetVehicle: 'Veículo Frota',
  ServiceOrder: 'Ordem de Serviço',
  CommissionRule: 'Regra Comissão',
  Tank: 'Tanque',
  User: 'Usuário',
}

function actionBadge(action: string) {
  const base = 'inline-block rounded-full px-2 py-0.5 text-[10px] font-medium'
  switch (action) {
    case 'CREATE':
    case 'OPEN':
      return `${base} bg-emerald-100 text-emerald-700 dark:bg-emerald-900/30 dark:text-emerald-400`
    case 'UPDATE':
    case 'UPDATE_STATIONS':
      return `${base} bg-blue-100 text-blue-700 dark:bg-blue-900/30 dark:text-blue-400`
    case 'DELETE':
      return `${base} bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400`
    case 'CLOSE':
      return `${base} bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400`
    default:
      return `${base} bg-slate-100 text-slate-600 dark:bg-slate-800 dark:text-slate-400`
  }
}

export function AuditLogPage() {
  const [page, setPage] = useState(0)
  const size = 50

  const { data, isLoading } = useQuery({
    queryKey: ['audit-logs', page],
    queryFn: () => listAuditLogs(page, size),
  })

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Audit Log" subtitle="Registro de ações do sistema" />
      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <Skeleton className="h-64 w-full" />
        ) : !data || data.content.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum registro encontrado.</p>
        ) : (
          <>
            <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b bg-slate-50 dark:bg-slate-800">
                    <th className="px-4 py-2 text-left text-slate-400">Data/Hora</th>
                    <th className="px-4 py-2 text-left text-slate-400">Usuário</th>
                    <th className="px-4 py-2 text-left text-slate-400">Ação</th>
                    <th className="px-4 py-2 text-left text-slate-400">Entidade</th>
                    <th className="px-4 py-2 text-left text-slate-400">Detalhes</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((log) => (
                    <tr key={log.id} className="border-b last:border-0 hover:bg-slate-50 dark:hover:bg-slate-800">
                      <td className="px-4 py-3 whitespace-nowrap text-slate-500 dark:text-slate-400">
                        {new Date(log.createdAt).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'medium' })}
                      </td>
                      <td className="px-4 py-3 font-medium text-slate-700 dark:text-slate-300">
                        {log.username}
                      </td>
                      <td className="px-4 py-3">
                        <span className={actionBadge(log.action)}>
                          {ACTION_LABELS[log.action] ?? log.action}
                        </span>
                      </td>
                      <td className="px-4 py-3 text-slate-500 dark:text-slate-400">
                        {ENTITY_LABELS[log.entityType] ?? log.entityType}
                      </td>
                      <td className="px-4 py-3 max-w-xs truncate text-slate-400 dark:text-slate-500" title={log.details ?? ''}>
                        {log.details ?? '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {data.totalPages > 1 && (
              <div className="mt-4 flex items-center justify-center gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  disabled={page === 0}
                  onClick={() => setPage((p) => p - 1)}
                >
                  Anterior
                </Button>
                <span className="text-xs text-slate-400">
                  Página {page + 1} de {data.totalPages}
                </span>
                <Button
                  size="sm"
                  variant="outline"
                  disabled={page >= data.totalPages - 1}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Próxima
                </Button>
              </div>
            )}
          </>
        )}
      </div>
    </div>
  )
}
