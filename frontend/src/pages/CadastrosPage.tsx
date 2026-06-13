import { Outlet } from 'react-router-dom'
import { CadastroSubnav } from '@/components/cadastros/CadastroSubnav'

export function CadastrosPage() {
  return (
    <div className="flex h-full">
      <CadastroSubnav />
      <div className="flex-1 overflow-auto">
        <Outlet />
      </div>
    </div>
  )
}
