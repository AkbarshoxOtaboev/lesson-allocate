import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 30000,
})

api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

let refreshing: Promise<string | null> | null = null

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean }
    const auth = useAuthStore()

    if (error.response?.status === 401 && original && !original._retry) {
      if (original.url?.includes('/auth/login') || original.url?.includes('/auth/refresh')) {
        return Promise.reject(error)
      }

      original._retry = true

      if (!refreshing) {
        refreshing = auth
          .refreshAccessToken()
          .finally(() => {
            refreshing = null
          })
      }

      const token = await refreshing
      if (token) {
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      }

      auth.clearSession()
      if (router.currentRoute.value.name !== 'Signin') {
        await router.push({ name: 'Signin', query: { redirect: router.currentRoute.value.fullPath } })
      }
    }

    return Promise.reject(error)
  },
)

export function getErrorMessage(error: unknown, fallback = 'So‘rov bajarilmadi'): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; error?: string } | undefined
    return data?.message || data?.error || error.message || fallback
  }
  if (error instanceof Error) return error.message
  return fallback
}

export default api
