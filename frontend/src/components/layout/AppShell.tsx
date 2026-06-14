import { Outlet } from 'react-router-dom'
import { Sidebar } from './Sidebar'
import { SidebarProvider, useSidebar } from '@/context/SidebarContext'

function AppShellInner() {
  const { isOpen, close } = useSidebar()

  return (
    <div className="flex h-screen overflow-hidden bg-slate-100 dark:bg-slate-950">
      {/* Mobile overlay */}
      {isOpen && (
        <div
          className="fixed inset-0 z-40 bg-black/40 md:hidden"
          onClick={close}
        />
      )}
      <Sidebar />
      <main className="flex flex-1 flex-col overflow-hidden">
        <Outlet />
      </main>
    </div>
  )
}

export function AppShell() {
  return (
    <SidebarProvider>
      <AppShellInner />
    </SidebarProvider>
  )
}
