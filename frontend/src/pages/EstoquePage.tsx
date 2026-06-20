import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Plus } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { Sheet, SheetContent, SheetHeader, SheetTitle } from '@/components/ui/sheet'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { useActiveStation } from '@/hooks/useActiveStation'
import { getTanks, createTank, registerDelivery, adjustTankLevel } from '@/api/tanks'
import type { TankData } from '@/api/tanks'
import { getFuels } from '@/api/fuels'
import { formatLiters } from '@/lib/utils'

function TankCard({ tank, onDelivery, onAdjust }: {
  tank: TankData
  onDelivery: (tank: TankData) => void
  onAdjust: (tank: TankData) => void
}) {
  const pct = tank.capacity > 0 ? Math.min((tank.currentLevel / tank.capacity) * 100, 100) : 0
  const isLow = tank.belowMinimum

  return (
    <div className={`rounded-lg border p-4 ${isLow ? 'border-red-300 bg-red-50 dark:border-red-800 dark:bg-red-950/30' : 'bg-white dark:bg-slate-900 dark:border-slate-700'}`}>
      <div className="flex items-center justify-between mb-2">
        <h3 className="font-semibold text-slate-800 dark:text-slate-200">{tank.name}</h3>
        <span className="text-xs text-slate-400">{tank.fuelName}</span>
      </div>
      <div className="h-24 w-full rounded bg-slate-100 dark:bg-slate-800 relative overflow-hidden mb-2">
        <div
          className={`absolute bottom-0 w-full transition-all ${isLow ? 'bg-red-400' : pct < 30 ? 'bg-amber-400' : 'bg-emerald-400'}`}
          style={{ height: `${pct}%` }}
        />
        <div className="absolute inset-0 flex items-center justify-center">
          <span className="text-sm font-bold text-slate-700 dark:text-slate-200 drop-shadow">{pct.toFixed(0)}%</span>
        </div>
      </div>
      <div className="flex justify-between text-xs text-slate-500 mb-3">
        <span>{formatLiters(tank.currentLevel)}</span>
        <span>/ {formatLiters(tank.capacity)}</span>
      </div>
      {isLow && <p className="text-xs text-red-600 font-medium mb-2">Abaixo do nível mínimo ({formatLiters(tank.minimumLevel)})</p>}
      <div className="flex gap-2">
        <Button size="sm" variant="outline" className="flex-1 text-xs" onClick={() => onDelivery(tank)}>Recebimento</Button>
        <Button size="sm" variant="ghost" className="flex-1 text-xs" onClick={() => onAdjust(tank)}>Ajuste</Button>
      </div>
    </div>
  )
}

export function EstoquePage() {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const [createSheet, setCreateSheet] = useState(false)
  const [deliverySheet, setDeliverySheet] = useState(false)
  const [adjustSheet, setAdjustSheet] = useState(false)
  const [selectedTank, setSelectedTank] = useState<TankData | null>(null)
  const [tankName, setTankName] = useState('')
  const [tankFuelId, setTankFuelId] = useState('')
  const [tankCapacity, setTankCapacity] = useState('')
  const [tankMinLevel, setTankMinLevel] = useState('')
  const [deliveryVolume, setDeliveryVolume] = useState('')
  const [deliveryNotes, setDeliveryNotes] = useState('')
  const [adjustLevel, setAdjustLevel] = useState('')
  const [adjustNotes, setAdjustNotes] = useState('')

  const { data: tanks = [], isLoading } = useQuery({
    queryKey: ['tanks', station?.id],
    queryFn: () => getTanks(station!.id),
    enabled: !!station,
  })

  const { data: fuels = [] } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
    enabled: createSheet,
  })

  const createMutation = useMutation({
    mutationFn: () => createTank({ stationId: station!.id, fuelId: tankFuelId, name: tankName, capacity: Number(tankCapacity), minimumLevel: Number(tankMinLevel) }),
    onSuccess: () => { toast.success('Tanque criado!'); qc.invalidateQueries({ queryKey: ['tanks'] }); setCreateSheet(false) },
    onError: () => toast.error('Erro ao criar tanque'),
  })

  const deliveryMutation = useMutation({
    mutationFn: () => registerDelivery(selectedTank!.id, { volumeLiters: Number(deliveryVolume), notes: deliveryNotes || undefined }),
    onSuccess: () => { toast.success('Recebimento registrado!'); qc.invalidateQueries({ queryKey: ['tanks'] }); setDeliverySheet(false) },
    onError: () => toast.error('Erro ao registrar recebimento'),
  })

  const adjustMutation = useMutation({
    mutationFn: () => adjustTankLevel(selectedTank!.id, { newLevel: Number(adjustLevel), notes: adjustNotes || undefined }),
    onSuccess: () => { toast.success('Nível ajustado!'); qc.invalidateQueries({ queryKey: ['tanks'] }); setAdjustSheet(false) },
    onError: () => toast.error('Erro ao ajustar nível'),
  })

  if (!station) {
    return <div className="flex flex-1 items-center justify-center text-sm text-slate-400">Selecione um posto.</div>
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Estoque" actions={
        <Button size="sm" className="bg-orange-600 hover:bg-orange-700" onClick={() => {
          setTankName(''); setTankFuelId(''); setTankCapacity(''); setTankMinLevel(''); setCreateSheet(true)
        }}>
          <Plus size={14} className="mr-1" /> Novo tanque
        </Button>
      } />

      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {[1, 2, 3].map(i => <Skeleton key={i} className="h-48" />)}
          </div>
        ) : tanks.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum tanque cadastrado.</p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {tanks.map(tank => (
              <TankCard
                key={tank.id}
                tank={tank}
                onDelivery={t => { setSelectedTank(t); setDeliveryVolume(''); setDeliveryNotes(''); setDeliverySheet(true) }}
                onAdjust={t => { setSelectedTank(t); setAdjustLevel(String(t.currentLevel)); setAdjustNotes(''); setAdjustSheet(true) }}
              />
            ))}
          </div>
        )}
      </div>

      <Sheet open={createSheet} onOpenChange={setCreateSheet}>
        <SheetContent className="w-[380px]">
          <SheetHeader><SheetTitle>Novo tanque</SheetTitle></SheetHeader>
          <div className="mt-4 flex flex-col gap-4">
            <div><Label>Nome</Label><Input value={tankName} onChange={e => setTankName(e.target.value)} placeholder="Ex: Tanque 1" /></div>
            <div>
              <Label>Combustível</Label>
              <Select value={tankFuelId} onValueChange={setTankFuelId}>
                <SelectTrigger><SelectValue placeholder="Selecionar" /></SelectTrigger>
                <SelectContent>
                  {fuels.map(f => <SelectItem key={f.id} value={f.id}>{f.name}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
            <div><Label>Capacidade (L)</Label><Input type="number" value={tankCapacity} onChange={e => setTankCapacity(e.target.value)} /></div>
            <div><Label>Nível mínimo (L)</Label><Input type="number" value={tankMinLevel} onChange={e => setTankMinLevel(e.target.value)} /></div>
            <Button onClick={() => createMutation.mutate()} disabled={createMutation.isPending || !tankName || !tankFuelId} className="bg-orange-600 hover:bg-orange-700">
              {createMutation.isPending ? 'Criando...' : 'Criar tanque'}
            </Button>
          </div>
        </SheetContent>
      </Sheet>

      <Sheet open={deliverySheet} onOpenChange={setDeliverySheet}>
        <SheetContent className="w-[340px]">
          <SheetHeader><SheetTitle>Recebimento — {selectedTank?.name}</SheetTitle></SheetHeader>
          <div className="mt-4 flex flex-col gap-4">
            <div><Label>Volume (L)</Label><Input type="number" step="0.001" value={deliveryVolume} onChange={e => setDeliveryVolume(e.target.value)} /></div>
            <div><Label>Observações</Label><Input value={deliveryNotes} onChange={e => setDeliveryNotes(e.target.value)} placeholder="Opcional" /></div>
            <Button onClick={() => deliveryMutation.mutate()} disabled={deliveryMutation.isPending || !deliveryVolume} className="bg-orange-600 hover:bg-orange-700">
              {deliveryMutation.isPending ? 'Registrando...' : 'Registrar recebimento'}
            </Button>
          </div>
        </SheetContent>
      </Sheet>

      <Sheet open={adjustSheet} onOpenChange={setAdjustSheet}>
        <SheetContent className="w-[340px]">
          <SheetHeader><SheetTitle>Ajuste — {selectedTank?.name}</SheetTitle></SheetHeader>
          <div className="mt-4 flex flex-col gap-4">
            <div><Label>Novo nível (L)</Label><Input type="number" step="0.01" value={adjustLevel} onChange={e => setAdjustLevel(e.target.value)} /></div>
            <div><Label>Motivo</Label><Input value={adjustNotes} onChange={e => setAdjustNotes(e.target.value)} placeholder="Ex: Aferição" /></div>
            <Button onClick={() => adjustMutation.mutate()} disabled={adjustMutation.isPending || !adjustLevel} className="bg-orange-600 hover:bg-orange-700">
              {adjustMutation.isPending ? 'Ajustando...' : 'Ajustar nível'}
            </Button>
          </div>
        </SheetContent>
      </Sheet>
    </div>
  )
}
