import { api } from '@/lib/api-client'
import type { AppUser, CreateUserRequest, UpdateUserRequest } from '@/types'

export function getUsers(): Promise<AppUser[]> {
  return api.get<AppUser[]>('/users')
}

export function getUser(id: string): Promise<AppUser> {
  return api.get<AppUser>(`/users/${id}`)
}

export function createUser(req: CreateUserRequest): Promise<AppUser> {
  return api.post<AppUser>('/users', req)
}

export function updateUser(id: string, req: UpdateUserRequest): Promise<AppUser> {
  return api.put<AppUser>(`/users/${id}`, req)
}
