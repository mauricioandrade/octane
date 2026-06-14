import { useState } from 'react'
import { useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus, CheckCircle, XCircle } from 'lucide-react'
import { Button } from '@/components/ui/button'
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '@/components/ui/dialog'
import { ServiceOrderStatusBadge } from './ServiceOrderStatusBadge'
import { AddItemSheet } from './AddItemSheet'
import { closeServiceOrder, cancelServiceOrder } from '@/api/service-orders'
import { formatBRL } from '@/lib/utils'
import type { ServiceOrder } from '@/types'

type Props = {
  order: ServiceOrder
}

export function ServiceOrderDetail({ order }: Props) {
  const qc = useQueryClient()
  const [addItemOpen, setAddItemOpen] = useState(false)
  const [closeDialogOpen, setCloseDialogOpen] = useState(false)
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false)

  const closeMutation = useMutation({
    mutationFn: () => closeServiceOrder(order.id),
    onSuccess: () => {
      toast.success('OS fechada com sucesso!')
      qc.invalidateQueries({ queryKey: ['service-order', order.id] })
      qc.invalidateQueries({ queryKey: ['service-orders'] })
      setCloseDialogOpen(false)
    },
    onError: () => {
      toast.error('Erro ao fechar OS')
    },
  })

  const cancelMutation = useMutation({
    mutationFn: () => cancelServiceOrder(order.id),
    onSuccess: () => {
      toast.success('OS cancelada.')
      qc.invalidateQueries({ queryKey: ['service-order', order.id] })
      qc.invalidateQueries({ queryKey: ['service-orders'] })
      setCancelDialogOpen(false)
    },
    onError: () => {
      toast.error('Erro ao cancelar OS')
    },
  })

  const isOpen = order.status === 'OPEN'

  return (
    <div className="flex flex-col gap-6">
      {/* Header card */}
      <div className="rounded-lg border bg-white dark:bg-slate-900 p-5">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="flex flex-col gap-1">
            <div className="flex items-center gap-2">
              <span className="text-2xl font-extrabold tracking-tight text-slate-800 dark:text-slate-100">
                {order.plate}
              </span>
              <ServiceOrderStatusBadge status={order.status} />
            </div>
            <p className="text-sm text-slate-500 dark:text-slate-400">
              Hodômetro: <span className="font-semibold">{order.odometer.toLocaleString('pt-BR')} km</span>
            </p>
            {order.customerName && (
              <p className="text-sm text-slate-500 dark:text-slate-400">
                Cliente:{' '}
                <span className="font-semibold text-slate-700 dark:text-slate-300">
                  {order.customerName}
                </span>
                {order.customerPhone && (
                  <span className="ml-1 text-slate-400">({order.customerPhone})</span>
                )}
              </p>
            )}
            <p className="text-xs text-slate-400 dark:text-slate-500">
              Aberta em: {new Date(order.openedAt).toLocaleString('pt-BR')}
            </p>
            {order.closedAt && (
              <p className="text-xs text-slate-400 dark:text-slate-500">
                Fechada em: {new Date(order.closedAt).toLocaleString('pt-BR')}
              </p>
            )}
            {order.cancelledAt && (
              <p className="text-xs text-slate-400 dark:text-slate-500">
                Cancelada em: {new Date(order.cancelledAt).toLocaleString('pt-BR')}
              </p>
            )}
            {order.notes && (
              <p className="mt-1 text-sm italic text-slate-400 dark:text-slate-500">
                "{order.notes}"
              </p>
            )}
          </div>

          {isOpen && (
            <div className="flex flex-wrap gap-2">
              <Button
                size="sm"
                className="bg-orange-600 hover:bg-orange-700"
                onClick={() => setAddItemOpen(true)}
              >
                <Plus size={14} className="mr-1" />
                Adicionar item
              </Button>
              <Button
                size="sm"
                variant="outline"
                className="border-green-600 text-green-700 hover:bg-green-50 dark:text-green-400 dark:hover:bg-green-900/20"
                onClick={() => setCloseDialogOpen(true)}
              >
                <CheckCircle size={14} className="mr-1" />
                Fechar OS
              </Button>
              <Button
                size="sm"
                variant="outline"
                className="border-red-400 text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/20"
                onClick={() => setCancelDialogOpen(true)}
              >
                <XCircle size={14} className="mr-1" />
                Cancelar OS
              </Button>
            </div>
          )}
        </div>
      </div>

      {/* Items table */}
      <div className="overflow-hidden rounded-lg border bg-white dark:bg-slate-900">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b bg-slate-50 dark:bg-slate-800">
              <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                Descrição
              </th>
              <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                Qtd
              </th>
              <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                Preço Unit.
              </th>
              <th className="px-4 py-2 text-right text-xs font-semibold uppercase text-slate-400 dark:text-slate-500">
                Total
              </th>
            </tr>
          </thead>
          <tbody>
            {order.items.length === 0 ? (
              <tr>
                <td colSpan={4} className="px-4 py-6 text-center text-sm text-slate-400">
                  Nenhum item adicionado ainda.
                </td>
              </tr>
            ) : (
              order.items.map((item) => (
                <tr key={item.id} className="border-b last:border-0">
                  <td className="px-4 py-3 text-slate-800 dark:text-slate-200">
                    {item.description}
                  </td>
                  <td className="px-4 py-3 text-right text-slate-500 dark:text-slate-400">
                    {item.quantity}
                  </td>
                  <td className="px-4 py-3 text-right text-slate-500 dark:text-slate-400">
                    {formatBRL(item.unitPrice)}
                  </td>
                  <td className="px-4 py-3 text-right font-semibold text-slate-800 dark:text-slate-200">
                    {formatBRL(item.totalPrice)}
                  </td>
                </tr>
              ))
            )}
          </tbody>
          <tfoot>
            <tr className="border-t bg-slate-50 dark:bg-slate-800">
              <td
                colSpan={3}
                className="px-4 py-3 text-right text-sm font-semibold text-slate-600 dark:text-slate-300"
              >
                Total geral
              </td>
              <td className="px-4 py-3 text-right text-base font-extrabold text-orange-600">
                {formatBRL(order.totalAmount)}
              </td>
            </tr>
          </tfoot>
        </table>
      </div>

      {/* Add item sheet */}
      <AddItemSheet
        open={addItemOpen}
        onOpenChange={setAddItemOpen}
        serviceOrderId={order.id}
      />

      {/* Close dialog */}
      <Dialog open={closeDialogOpen} onOpenChange={setCloseDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Fechar Ordem de Serviço</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-slate-600 dark:text-slate-400">
            Tem certeza que deseja fechar a OS da placa{' '}
            <span className="font-semibold">{order.plate}</span>? Após fechada, não será possível
            adicionar mais itens.
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCloseDialogOpen(false)}>
              Cancelar
            </Button>
            <Button
              className="bg-green-600 hover:bg-green-700"
              disabled={closeMutation.isPending}
              onClick={() => closeMutation.mutate()}
            >
              {closeMutation.isPending ? 'Fechando…' : 'Confirmar fechamento'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Cancel dialog */}
      <Dialog open={cancelDialogOpen} onOpenChange={setCancelDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Cancelar Ordem de Serviço</DialogTitle>
          </DialogHeader>
          <p className="text-sm text-slate-600 dark:text-slate-400">
            Tem certeza que deseja cancelar a OS da placa{' '}
            <span className="font-semibold">{order.plate}</span>? Esta ação não pode ser desfeita.
          </p>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCancelDialogOpen(false)}>
              Voltar
            </Button>
            <Button
              variant="destructive"
              disabled={cancelMutation.isPending}
              onClick={() => cancelMutation.mutate()}
            >
              {cancelMutation.isPending ? 'Cancelando…' : 'Confirmar cancelamento'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
