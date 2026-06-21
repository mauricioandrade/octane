import { Navigate } from 'react-router-dom'
import { useAuth } from '@/context/AuthContext'
import type { UserRole } from '@/types'

type Props = {
  roles: UserRole[]
  children: React.ReactNode
}

export function RequireRole({ roles, children }: Props) {
  const { authState } = useAuth()

  if (authState.status !== 'authenticated') {
    return <Navigate to="/" replace />
  }

  if (!roles.includes(authState.user.role)) {
    return <Navigate to="/dashboard" replace />
  }

  return <>{children}</>
}
