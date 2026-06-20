import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'sonner'
import { AppShell } from '@/components/layout/AppShell'
import { StationProvider } from '@/context/StationContext'
import { AuthProvider, useAuth } from '@/context/AuthContext'
import { PistaPage } from '@/pages/PistaPage'
import { PrecosPage } from '@/pages/PrecosPage'
import { CadastrosPage } from '@/pages/CadastrosPage'
import { PostosPage } from '@/pages/PostosPage'
import { BombasPage } from '@/pages/BombasPage'
import { BicosPage } from '@/pages/BicosPage'
import { CombustiveisPage } from '@/pages/CombustiveisPage'
import { HistoricoPage } from '@/pages/HistoricoPage'
import { LoginPage } from '@/pages/LoginPage'
import { FrotaClientesPage } from '@/pages/FrotaClientesPage'
import { FrotaClienteDetailPage } from '@/pages/FrotaClienteDetailPage'
import { FrotaVeiculosPage } from '@/pages/FrotaVeiculosPage'
import { FrotaMotoristasPage } from '@/pages/FrotaMotoristasPage'
import { FrotaRelatorioPage } from '@/pages/FrotaRelatorioPage'
import { OrdensServicoPage } from '@/pages/OrdensServicoPage'
import { OrdemServicoDetailPage } from '@/pages/OrdemServicoDetailPage'
import { VehicleHistoryPage } from '@/pages/VehicleHistoryPage'
import { ComissaoRegrasPage } from '@/pages/ComissaoRegrasPage'
import { ComissaoEntradasPage } from '@/pages/ComissaoEntradasPage'
import { UsuariosPage } from '@/pages/UsuariosPage'
import { DashboardPage } from '@/pages/DashboardPage'
import { RelatorioVendasPage } from '@/pages/RelatorioVendasPage'
import { RelatorioTurnosPage } from '@/pages/RelatorioTurnosPage'

function ProtectedApp() {
  const { authState } = useAuth()

  if (authState.status === 'loading') {
    return (
      <div className="flex h-screen items-center justify-center bg-slate-100 dark:bg-slate-950">
        <span className="text-sm text-slate-400">Carregando…</span>
      </div>
    )
  }

  if (authState.status === 'unauthenticated') {
    return <LoginPage />
  }

  return (
    <StationProvider>
      <Routes>
        <Route element={<AppShell />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/pista" element={<PistaPage />} />
          <Route path="/precos" element={<PrecosPage />} />
          <Route path="/cadastros" element={<CadastrosPage />}>
            <Route index element={<Navigate to="/cadastros/postos" replace />} />
            <Route path="postos" element={<PostosPage />} />
            <Route path="bombas" element={<BombasPage />} />
            <Route path="bicos" element={<BicosPage />} />
            <Route path="combustiveis" element={<CombustiveisPage />} />
          </Route>
          <Route path="/historico" element={<HistoricoPage />} />
          <Route path="/frota" element={<Navigate to="/frota/clientes" replace />} />
          <Route path="/frota/clientes" element={<FrotaClientesPage />} />
          <Route path="/frota/clientes/:clientId" element={<FrotaClienteDetailPage />} />
          <Route path="/frota/veiculos" element={<FrotaVeiculosPage />} />
          <Route path="/frota/motoristas" element={<FrotaMotoristasPage />} />
          <Route path="/frota/relatorio" element={<FrotaRelatorioPage />} />
          <Route path="/os/historico" element={<VehicleHistoryPage />} />
          <Route path="/os/:id" element={<OrdemServicoDetailPage />} />
          <Route path="/os" element={<OrdensServicoPage />} />
          <Route path="/comissao/regras" element={<ComissaoRegrasPage />} />
          <Route path="/comissao/entradas" element={<ComissaoEntradasPage />} />
          <Route path="/usuarios" element={<UsuariosPage />} />
          <Route path="/relatorios/vendas" element={<RelatorioVendasPage />} />
          <Route path="/relatorios/turnos" element={<RelatorioTurnosPage />} />
        </Route>
      </Routes>
    </StationProvider>
  )
}

export function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <ProtectedApp />
        <Toaster richColors position="top-right" />
      </AuthProvider>
    </BrowserRouter>
  )
}
