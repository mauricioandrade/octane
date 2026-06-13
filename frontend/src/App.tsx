import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Toaster } from 'sonner'
import { AppShell } from '@/components/layout/AppShell'
import { StationProvider } from '@/context/StationContext'
import { PistaPage } from '@/pages/PistaPage'
import { PrecosPage } from '@/pages/PrecosPage'
import { CadastrosPage } from '@/pages/CadastrosPage'
import { PostosPage } from '@/pages/PostosPage'
import { BombasPage } from '@/pages/BombasPage'
import { BicosPage } from '@/pages/BicosPage'
import { CombustiveisPage } from '@/pages/CombustiveisPage'
import { HistoricoPage } from '@/pages/HistoricoPage'

export function App() {
  return (
    <BrowserRouter>
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
        <Toaster richColors position="top-right" />
      </StationProvider>
    </BrowserRouter>
  )
}
