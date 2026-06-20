import { api } from '@/lib/api-client'

export interface AuthUser {
  username: string
  name: string
  role: 'ADMIN' | 'MANAGER' | 'ATTENDANT'
}

export function loginUser(username: string, password: string): Promise<AuthUser> {
  return api.post<AuthUser>('/auth/login', { username, password })
}

export function logoutUser(): Promise<void> {
  return api.post<void>('/auth/logout')
}

export function getMe(): Promise<AuthUser> {
  return api.get<AuthUser>('/auth/me')
}
