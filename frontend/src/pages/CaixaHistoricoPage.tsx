import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/ui/skeleton'
import { Button } from '@/components/ui/button'
import { TopBar } from '@/components/layout/TopBar'
import { useActiveStation } from '@/hooks/useActiveStation'
import { listCashRegisters } from '@/api/cash-registers'
import { formatBRL } from '@/lib/utils'

export function CaixaHistoricoPage() {
  const { station } = useActiveStation()
  const [page, setPage] = useState(0)
  const size = 20

  const { data, isLoading } = useQuery({
    queryKey: ['cash-registers-history', station?.id, page],
    queryFn: () => listCashRegisters(station!.id, page, size),
    enabled: !!station,
  })

  if (!station) {
    return (
      <div className="flex flex-1 items-center justify-center text-sm text-slate-400">
        Selecione um posto.
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Histórico de Caixa" />
      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <Skeleton className="h-64 w-full" />
        ) : !data || data.content.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum caixa fechado encontrado.</p>
        ) : (
          <>
            <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
              <table className="w-full text-xs">
                <thead>
                  <tr className="border-b bg-slate-50 dark:bg-slate-800">
                    <th className="px-4 py-2 text-left text-slate-400">Abertura</th>
                    <th className="px-4 py-2 text-left text-slate-400">Fechamento</th>
                    <th className="px-4 py-2 text-right text-slate-400">Saldo inicial</th>
                    <th className="px-4 py-2 text-right text-slate-400">Saldo final</th>
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((r) => (
                    <tr key={r.id} className="border-b last:border-0 hover:bg-slate-50 dark:hover:bg-slate-800">
                      <td className="px-4 py-3 text-slate-700 dark:text-slate-300">
                        {new Date(r.openedAt).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}
                      </td>
                      <td className="px-4 py-3 text-slate-500">
                        {r.closedAt ? new Date(r.closedAt).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' }) : '—'}
                      </td>
                      <td className="px-4 py-3 text-right text-slate-700 dark:text-slate-300">
                        {formatBRL(r.openingBalance)}
                      </td>
                      <td className="px-4 py-3 text-right font-medium text-slate-700 dark:text-slate-300">
                        {r.closingBalance != null ? formatBRL(r.closingBalance) : '—'}
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
