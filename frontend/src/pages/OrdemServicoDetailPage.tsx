import { useParams, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { ServiceOrderDetail } from '@/components/os/ServiceOrderDetail'
import { getServiceOrder } from '@/api/service-orders'

export function OrdemServicoDetailPage() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()

  const { data: order, isLoading } = useQuery({
    queryKey: ['service-order', id],
    queryFn: () => getServiceOrder(id!),
    enabled: !!id,
  })

  if (isLoading) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Ordem de Serviço" />
        <div className="flex-1 p-6">
          <Skeleton className="mb-4 h-24 w-full" />
          <Skeleton className="h-48 w-full" />
        </div>
      </div>
    )
  }

  if (!order) {
    return (
      <div className="flex flex-1 flex-col overflow-hidden">
        <TopBar title="Ordem de Serviço" />
        <div className="flex flex-1 items-center justify-center">
          <p className="text-sm text-slate-400">Ordem de serviço não encontrada.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar
        title={`OS — ${order.plate}`}
        subtitle={order.stationName}
        actions={
          <Button variant="ghost" size="sm" onClick={() => navigate('/os')}>
            <ArrowLeft size={14} className="mr-1" />
            Voltar
          </Button>
        }
      />

      <div className="flex-1 overflow-auto p-6">
        <ServiceOrderDetail order={order} />
      </div>
    </div>
  )
}
