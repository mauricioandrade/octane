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
          <Route index element={<Navigate to="/pista" replace />} />
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
