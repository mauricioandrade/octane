# Task 12 — Cadastros: CombustiveisPage

**Files:**
- Modify: `frontend/src/pages/CombustiveisPage.tsx`

Combustíveis são globais (não dependem do posto ativo). Sem criação de novos — apenas ativar/inativar via toggle inline.

---

- [ ] **Step 1: Atualizar `frontend/src/pages/CombustiveisPage.tsx`**

```typescript
import { useQuery } from '@tanstack/react-query'
import { Skeleton } from '@/components/ui/skeleton'
import { TopBar } from '@/components/layout/TopBar'
import { StatusToggle } from '@/components/cadastros/StatusToggle'
import { getFuels, patchFuelStatus } from '@/api/fuels'

export function CombustiveisPage() {
  const { data: fuels = [], isLoading } = useQuery({
    queryKey: ['fuels'],
    queryFn: () => getFuels(),
  })

  return (
    <div className="flex flex-1 flex-col overflow-hidden">
      <TopBar title="Combustíveis" subtitle="Lista global — ative ou inative conforme disponibilidade" />

      <div className="flex-1 overflow-auto p-6">
        {isLoading ? (
          <div className="flex flex-col gap-2">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : fuels.length === 0 ? (
          <p className="text-sm text-slate-400">Nenhum combustível cadastrado no sistema.</p>
        ) : (
          <div className="overflow-hidden rounded-lg border bg-white">
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b bg-slate-50">
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">
                    Nome
                  </th>
                  <th className="px-4 py-2 text-left text-xs font-semibold uppercase text-slate-400">
                    Unidade
                  </th>
                  <th className="px-4 py-2 text-center text-xs font-semibold uppercase text-slate-400">
                    Status
                  </th>
                </tr>
              </thead>
              <tbody>
                {fuels.map((fuel) => (
                  <tr key={fuel.id} className="border-b last:border-0">
                    <td className="px-4 py-3 font-semibold text-slate-800">{fuel.name}</td>
                    <td className="px-4 py-3 text-slate-500">{fuel.unit}</td>
                    <td className="px-4 py-3 text-center">
                      <StatusToggle
                        id={fuel.id}
                        active={fuel.active}
                        queryKeys={[['fuels']]}
                        onToggle={(id, active) => patchFuelStatus(id, active)}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
```

- [ ] **Step 2: Verificar compilação**

```bash
cd frontend
npm run build
```

Esperado: zero erros TypeScript.

- [ ] **Step 3: Teste manual (backend rodando)**

1. Navegar para `/cadastros/combustiveis`
2. Lista todos os combustíveis do sistema (Gasolina Comum, Etanol, Diesel S10, etc.)
3. Badge de status clicável → alterna ativo/inativo
4. Sem botão de criação (combustíveis são cadastrados via seed/migração no backend)

- [ ] **Step 4: Commit**

```bash
cd ..
git add frontend/src/pages/CombustiveisPage.tsx
git commit -m "feat(frontend): cadastro de combustíveis com toggle de status"
```
