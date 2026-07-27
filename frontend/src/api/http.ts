import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { useAuthStore } from '@/stores/auth'
import router from '@/router'
import { showError } from '@/utils/swal'

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
let lastAlertAt = 0
let lastAlertMessage = ''

function isAuthEndpoint(url?: string) {
  return Boolean(url && (url.includes('/auth/login') || url.includes('/auth/refresh')))
}

function notifyApiError(status?: number, message?: string) {
  if (!status || status < 400) return
  // Login sahifasidagi xatolarni interceptor emas, forma ko'rsatadi
  if (router.currentRoute.value.name === 'Signin') return

  const text = localizeErrorMessage(message, status)
  const now = Date.now()
  if (text === lastAlertMessage && now - lastAlertAt < 1500) return
  lastAlertMessage = text
  lastAlertAt = now

  const title =
    status === 403
      ? 'Ruxsat yo‘q'
      : status === 401
        ? 'Sessiya tugadi'
        : status >= 500
          ? 'Server xatosi'
          : 'Xatolik'

  void showError(text, title)
}

function localizeErrorMessage(message: string | undefined, status: number): string {
  const raw = (message || '').trim()
  const lower = raw.toLowerCase()

  if (
    status === 403 ||
    lower.includes('access is denied') ||
    lower.includes('access denied') ||
    lower.includes('forbidden')
  ) {
    return "Ruxsat yo'q: bu amalni bajarishga ruxsatingiz yetarli emas"
  }
  if (status === 401 || lower.includes('unauthorized')) {
    return 'Sessiya muddati tugagan. Qayta kiring'
  }
  if (status >= 500 || lower.includes('internal server error')) {
    return 'Server xatosi yuz berdi'
  }
  return raw || 'So‘rov bajarilmadi'
}

api.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as InternalAxiosRequestConfig & { _retry?: boolean }
    const auth = useAuthStore()
    const status = error.response?.status
    const data = error.response?.data as { message?: string; error?: string } | undefined
    const message = data?.message || data?.error || error.message

    if (status === 401 && original && !original._retry) {
      if (isAuthEndpoint(original.url)) {
        return Promise.reject(error)
      }

      original._retry = true

      if (!auth.refreshToken) {
        auth.clearSession()
        if (router.currentRoute.value.name !== 'Signin') {
          await auth.redirectToLogin(router.currentRoute.value.fullPath)
        }
        notifyApiError(status, message)
        return Promise.reject(error)
      }

      if (!refreshing) {
        refreshing = auth.refreshAccessToken().finally(() => {
          refreshing = null
        })
      }

      const token = await refreshing
      if (token) {
        original.headers.Authorization = `Bearer ${token}`
        return api(original)
      }

      if (router.currentRoute.value.name !== 'Signin') {
        await auth.redirectToLogin(router.currentRoute.value.fullPath)
      }
      notifyApiError(status, message)
      return Promise.reject(error)
    }

    if (status && (status === 403 || status === 401 || status >= 500)) {
      notifyApiError(status, message)
    }

    return Promise.reject(error)
  },
)

export function getErrorMessage(error: unknown, fallback = 'So‘rov bajarilmadi'): string {
  if (axios.isAxiosError(error)) {
    const status = error.response?.status || 0
    const data = error.response?.data as { message?: string; error?: string } | undefined
    return localizeErrorMessage(data?.message || data?.error || error.message, status) || fallback
  }
  if (error instanceof Error) return localizeErrorMessage(error.message, 0) || fallback
  return fallback
}

export default api
