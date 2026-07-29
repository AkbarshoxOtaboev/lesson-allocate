import api from './http'
import type { LoginResponse, User } from '@/types/api'

export interface LoginPayload {
  username: string
  password: string
}

export interface ProfileUpdatePayload {
  fullName?: string
  phone?: string | null
  bio?: string | null
  country?: string | null
  city?: string | null
  region?: string | null
  postalCode?: string | null
  taxId?: string | null
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

export const authApi = {
  login(payload: LoginPayload) {
    return api.post<LoginResponse>('/auth/login', payload)
  },
  refresh(refreshToken: string) {
    return api.post<LoginResponse>('/auth/refresh', { refreshToken })
  },
  logout() {
    return api.post('/auth/logout')
  },
  me() {
    return api.get<User>('/auth/me')
  },
  updateProfile(payload: ProfileUpdatePayload) {
    return api.put<User>('/auth/profile', payload)
  },
  changePassword(payload: ChangePasswordPayload) {
    return api.put('/auth/change-password', payload)
  },
}
