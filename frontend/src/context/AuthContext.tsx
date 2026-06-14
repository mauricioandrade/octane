import { createContext, useContext, useEffect, useState, type ReactNode } from 'react'
import { getMe, loginUser, logoutUser } from '@/api/auth'
import type { AuthUser } from '@/api/auth'

type AuthState =
  | { status: 'loading' }
  | { status: 'authenticated'; user: AuthUser }
  | { status: 'unauthenticated' }

type AuthContextValue = {
  authState: AuthState
  login: (username: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

const AuthContext = createContext<AuthContextValue | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authState, setAuthState] = useState<AuthState>({ status: 'loading' })

  useEffect(() => {
    getMe()
      .then((user) => setAuthState({ status: 'authenticated', user }))
      .catch(() => setAuthState({ status: 'unauthenticated' }))
  }, [])

  useEffect(() => {
    function handleUnauthorized() {
      setAuthState({ status: 'unauthenticated' })
    }
    window.addEventListener('auth:unauthorized', handleUnauthorized)
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized)
  }, [])

  async function login(username: string, password: string) {
    const user = await loginUser(username, password)
    setAuthState({ status: 'authenticated', user })
  }

  async function logout() {
    await logoutUser().catch(() => {})
    setAuthState({ status: 'unauthenticated' })
  }

  return (
    <AuthContext.Provider value={{ authState, login, logout }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
