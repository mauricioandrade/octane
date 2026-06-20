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
import { openCashRegister, closeCashRegister, addCashMovement, getCashRegisterSummary, listCashRegisters } from '@/api/cash-registers'
import type { CashRegisterSummary, CashRegisterData } from '@/api/cash-registers'
import { formatBRL } from '@/lib/utils'

const CATEGORY_LABELS: Record<string, string> = {
  FUEL_SALE: 'Venda combustível',
  SERVICE_ORDER: 'Ordem de serviço',
  SUPPLY_PURCHASE: 'Compra suprimentos',
  SALARY_ADVANCE: 'Adiantamento salário',
  OTHER: 'Outros',
}

export function CaixaPage() {
  const { station } = useActiveStation()
  const qc = useQueryClient()
  const [openSheet, setOpenSheet] = useState(false)
  const [movementSheet, setMovementSheet] = useState(false)
  const [closeSheet, setCloseSheet] = useState(false)
  const [openingBalance, setOpeningBalance] = useState('')
  const [closingBalance, setClosingBalance] = useState('')
  const [movType, setMovType] = useState('INCOME')
  const [movCategory, setMovCategory] = useState('OTHER')
  const [movDescription, setMovDescription] = useState('')
  const [movAmount, setMovAmount] = useState('')

  const { data: summary, isLoading } = useQuery<CashRegisterSummary | null>({
    queryKey: ['cash-register', station?.id],
    queryFn: async () => {
      const page = await listCashRegisters(station!.id, 0, 50)
      const openRegister = page.content.find((r: CashRegisterData) => r.status === 'OPEN')
      if (!openRegister) return null
      return getCashRegisterSummary(openRegister.id)
    },
    enabled: !!station,
  })

  const openMutation = useMutation({
    mutationFn: () => openCashRegister({ stationId: station!.id, openingBalance: Number(openingBalance) }),
    onSuccess: () => { toast.success('Caixa aberto!'); qc.invalidateQueries({ queryKey: ['cash-register'] }); setOpenSheet(false) },
    onError: () => toast.error('Erro ao abrir caixa'),
  })

  const closeMutation = useMutation({
    mutationFn: () => closeCashRegister(summary!.register.id, Number(closingBalance)),
    onSuccess: () => { toast.success('Caixa fechado!'); qc.invalidateQueries({ queryKey: ['cash-register'] }); setCloseSheet(false) },
    onError: () => toast.error('Erro ao fechar caixa'),
  })

  const movMutation = useMutation({
    mutationFn: () => addCashMovement(summary!.register.id, {
      type: movType, category: movCategory, description: movDescription || undefined, amount: Number(movAmount),
    }),
    onSuccess: () => { toast.success('Movimentação adicionada!'); qc.invalidateQueries({ queryKey: ['cash-register'] }); setMovementSheet(false) },
    onError: () => toast.error('Erro ao adicionar movimentação'),
  })

  if (!station) {
    return <div className="flex flex-1 items-center justify-center text-sm text-slate-400">Selecione um posto.</div>
  }

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Caixa" actions={
        summary ? (
          <>
            <Button size="sm" onClick={() => { setMovAmount(''); setMovDescription(''); setMovementSheet(true) }}>
              <Plus size={14} className="mr-1" /> Movimentação
            </Button>
            <Button size="sm" variant="destructive" onClick={() => { setClosingBalance(''); setCloseSheet(true) }}>
              Fechar caixa
            </Button>
          </>
        ) : undefined
      } />

      <div className="flex flex-1 flex-col gap-4 overflow-auto p-6">
        {isLoading ? (
          <Skeleton className="h-48 w-full" />
        ) : !summary ? (
          <div className="flex flex-1 flex-col items-center justify-center gap-4">
            <p className="text-slate-500">Nenhum caixa aberto.</p>
            <Button onClick={() => { setOpeningBalance(''); setOpenSheet(true) }} className="bg-orange-600 hover:bg-orange-700">
              Abrir caixa
            </Button>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
              <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
                <p className="text-[10px] uppercase text-slate-400">Saldo inicial</p>
                <p className="mt-1 text-lg font-bold text-slate-900 dark:text-slate-100">{formatBRL(summary.register.openingBalance)}</p>
              </div>
              <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
                <p className="text-[10px] uppercase text-slate-400">Entradas</p>
                <p className="mt-1 text-lg font-bold text-emerald-600">{formatBRL(summary.totalIncome)}</p>
              </div>
              <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
                <p className="text-[10px] uppercase text-slate-400">Saídas</p>
                <p className="mt-1 text-lg font-bold text-red-600">{formatBRL(summary.totalExpense)}</p>
              </div>
              <div className="rounded-lg border bg-white dark:bg-slate-900 p-4">
                <p className="text-[10px] uppercase text-slate-400">Saldo atual</p>
                <p className="mt-1 text-lg font-bold text-slate-900 dark:text-slate-100">{formatBRL(summary.balance)}</p>
              </div>
            </div>

            <div className="rounded-lg border bg-white dark:bg-slate-900 overflow-hidden">
              <h3 className="px-4 py-3 text-xs font-semibold uppercase text-slate-400 border-b dark:border-slate-700">Movimentações</h3>
              {summary.movements.length === 0 ? (
                <p className="p-4 text-sm text-slate-400">Nenhuma movimentação.</p>
              ) : (
                <table className="w-full text-xs">
                  <thead>
                    <tr className="border-b bg-slate-50 dark:bg-slate-800">
                      <th className="px-4 py-2 text-left text-slate-400">Tipo</th>
                      <th className="px-4 py-2 text-left text-slate-400">Categoria</th>
                      <th className="px-4 py-2 text-left text-slate-400">Descrição</th>
                      <th className="px-4 py-2 text-right text-slate-400">Valor</th>
                      <th className="px-4 py-2 text-right text-slate-400">Data</th>
                    </tr>
                  </thead>
                  <tbody>
                    {summary.movements.map(m => (
                      <tr key={m.id} className="border-b last:border-0">
                        <td className="px-4 py-2">
                          <span className={m.type === 'INCOME' ? 'text-emerald-600 font-medium' : 'text-red-600 font-medium'}>
                            {m.type === 'INCOME' ? 'Entrada' : 'Saída'}
                          </span>
                        </td>
                        <td className="px-4 py-2 text-slate-500">{CATEGORY_LABELS[m.category] ?? m.category}</td>
                        <td className="px-4 py-2 text-slate-700 dark:text-slate-300">{m.description ?? '—'}</td>
                        <td className="px-4 py-2 text-right font-medium text-slate-700 dark:text-slate-300">{formatBRL(m.amount)}</td>
                        <td className="px-4 py-2 text-right text-slate-500">
                          {new Date(m.createdAt).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </>
        )}
      </div>

      <Sheet open={openSheet} onOpenChange={setOpenSheet}>
        <SheetContent className="w-[340px]">
          <SheetHeader><SheetTitle>Abrir caixa</SheetTitle></SheetHeader>
          <div className="mt-4 flex flex-col gap-4">
            <div>
              <Label>Saldo inicial</Label>
              <Input type="number" step="0.01" value={openingBalance} onChange={e => setOpeningBalance(e.target.value)} placeholder="0.00" />
            </div>
            <Button onClick={() => openMutation.mutate()} disabled={openMutation.isPending || !openingBalance} className="bg-orange-600 hover:bg-orange-700">
              {openMutation.isPending ? 'Abrindo...' : 'Abrir caixa'}
            </Button>
          </div>
        </SheetContent>
      </Sheet>

      <Sheet open={closeSheet} onOpenChange={setCloseSheet}>
        <SheetContent className="w-[340px]">
          <SheetHeader><SheetTitle>Fechar caixa</SheetTitle></SheetHeader>
          <div className="mt-4 flex flex-col gap-4">
            <div>
              <Label>Saldo de fechamento</Label>
              <Input type="number" step="0.01" value={closingBalance} onChange={e => setClosingBalance(e.target.value)} placeholder="0.00" />
            </div>
            <Button variant="destructive" onClick={() => closeMutation.mutate()} disabled={closeMutation.isPending || !closingBalance}>
              {closeMutation.isPending ? 'Fechando...' : 'Fechar caixa'}
            </Button>
          </div>
        </SheetContent>
      </Sheet>

      <Sheet open={movementSheet} onOpenChange={setMovementSheet}>
        <SheetContent className="w-[380px]">
          <SheetHeader><SheetTitle>Nova movimentação</SheetTitle></SheetHeader>
          <div className="mt-4 flex flex-col gap-4">
            <div>
              <Label>Tipo</Label>
              <Select value={movType} onValueChange={setMovType}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  <SelectItem value="INCOME">Entrada</SelectItem>
                  <SelectItem value="EXPENSE">Saída</SelectItem>
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label>Categoria</Label>
              <Select value={movCategory} onValueChange={setMovCategory}>
                <SelectTrigger><SelectValue /></SelectTrigger>
                <SelectContent>
                  {Object.entries(CATEGORY_LABELS).map(([k, v]) => <SelectItem key={k} value={k}>{v}</SelectItem>)}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label>Descrição</Label>
              <Input value={movDescription} onChange={e => setMovDescription(e.target.value)} placeholder="Opcional" />
            </div>
            <div>
              <Label>Valor (R$)</Label>
              <Input type="number" step="0.01" value={movAmount} onChange={e => setMovAmount(e.target.value)} placeholder="0.00" />
            </div>
            <Button onClick={() => movMutation.mutate()} disabled={movMutation.isPending || !movAmount} className="bg-orange-600 hover:bg-orange-700">
              {movMutation.isPending ? 'Salvando...' : 'Adicionar'}
            </Button>
          </div>
        </SheetContent>
      </Sheet>
    </div>
  )
}
