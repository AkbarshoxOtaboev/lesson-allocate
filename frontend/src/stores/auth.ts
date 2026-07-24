import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/auth'
import type { User } from '@/types/api'

const ACCESS_KEY = 'urspi_access_token'
const REFRESH_KEY = 'urspi_refresh_token'
const USER_KEY = 'urspi_user'

function readJson<T>(key: string): T | null {
  try {
    const raw = localStorage.getItem(key)
    return raw ? (JSON.parse(raw) as T) : null
  } catch {
    return null
  }
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem(ACCESS_KEY))
  const refreshToken = ref<string | null>(localStorage.getItem(REFRESH_KEY))
  const user = ref<User | null>(readJson<User>(USER_KEY))
  const loading = ref(false)

  const isAuthenticated = computed(() => Boolean(accessToken.value))
  const displayName = computed(
    () => user.value?.fullName || user.value?.username || 'Foydalanuvchi',
  )

  function persist() {
    if (accessToken.value) localStorage.setItem(ACCESS_KEY, accessToken.value)
    else localStorage.removeItem(ACCESS_KEY)

    if (refreshToken.value) localStorage.setItem(REFRESH_KEY, refreshToken.value)
    else localStorage.removeItem(REFRESH_KEY)

    if (user.value) localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    else localStorage.removeItem(USER_KEY)
  }

  function setSession(tokens: {
    accessToken: string
    refreshToken: string
    user?: User | null
    username?: string
    fullName?: string
  }) {
    accessToken.value = tokens.accessToken
    refreshToken.value = tokens.refreshToken
    if (tokens.user) {
      user.value = tokens.user
    } else if (tokens.username || tokens.fullName) {
      user.value = {
        id: user.value?.id ?? 0,
        username: tokens.username || user.value?.username || '',
        fullName: tokens.fullName || tokens.username,
        status: 'ACTIVE',
      }
    }
    persist()
  }

  function clearSession() {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    persist()
  }

  async function login(username: string, password: string) {
    loading.value = true
    try {
      const { data } = await authApi.login({ username, password })
      setSession({
        accessToken: data.accessToken,
        refreshToken: data.refreshToken,
        user: data.user,
        username: data.username || username,
        fullName: data.fullName,
      })
      return data
    } finally {
      loading.value = false
    }
  }

  async function refreshAccessToken(): Promise<string | null> {
    if (!refreshToken.value) return null
    try {
      const { data } = await authApi.refresh(refreshToken.value)
      accessToken.value = data.accessToken
      if (data.refreshToken) refreshToken.value = data.refreshToken
      persist()
      return data.accessToken
    } catch {
      clearSession()
      return null
    }
  }

  async function logout() {
    try {
      if (accessToken.value) await authApi.logout()
    } catch {
      // ignore network errors on logout
    } finally {
      clearSession()
    }
  }

  return {
    accessToken,
    refreshToken,
    user,
    loading,
    isAuthenticated,
    displayName,
    login,
    logout,
    refreshAccessToken,
    clearSession,
    setSession,
  }
})
