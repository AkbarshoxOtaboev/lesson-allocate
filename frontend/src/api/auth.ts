import api from './http'
import type { LoginResponse } from '@/types/api'

export interface LoginPayload {
  username: string
  password: string
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
}
