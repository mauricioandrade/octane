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

export function getUserStations(userId: string): Promise<string[]> {
  return api.get<string[]>(`/users/${userId}/stations`)
}

export function updateUserStations(userId: string, stationIds: string[]): Promise<string[]> {
  return api.put<string[]>(`/users/${userId}/stations`, { stationIds })
}
