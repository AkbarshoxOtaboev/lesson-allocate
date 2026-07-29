import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { authApi } from '@/api/auth'
import type { User } from '@/types/api'
import { isAccessTokenExpired, msUntilRefresh } from '@/utils/jwt'
import router from '@/router'

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

  let refreshTimer: ReturnType<typeof setTimeout> | null = null
  let refreshInFlight: Promise<string | null> | null = null
  let visibilityHandler: (() => void) | null = null
  let activityHandler: (() => void) | null = null
  let lastActivityRefreshAt = 0

  const isAuthenticated = computed(() => Boolean(accessToken.value || refreshToken.value))
  const displayName = computed(
    () => user.value?.fullName || user.value?.username || 'Foydalanuvchi',
  )
  const isSuperAdmin = computed(() =>
    Boolean(user.value?.roles?.some((r) => r.name === 'SUPER_ADMIN')),
  )
  const isAdmin = computed(() => Boolean(user.value?.roles?.some((r) => r.name === 'ADMIN')))
  const isDekan = computed(() => Boolean(user.value?.roles?.some((r) => r.name === 'DEKAN')))
  const isKafedra = computed(() => Boolean(user.value?.roles?.some((r) => r.name === 'KAFEDRA')))
  const hasFullAccess = computed(() => isSuperAdmin.value || isAdmin.value)
  const facultyId = computed(() => user.value?.facultyId ?? null)
  const departmentId = computed(() => user.value?.departmentId ?? null)

  function hasRole(roleName: string) {
    return Boolean(user.value?.roles?.some((r) => r.name === roleName))
  }

  /** Katalog API so'rovlari uchun fakultet/kafedra filtri */
  function catalogScopeParams(): Record<string, number> {
    const params: Record<string, number> = {}
    if (isKafedra.value && departmentId.value) {
      params.departmentId = departmentId.value
      if (facultyId.value) params.facultyId = facultyId.value
    } else if (isDekan.value && facultyId.value) {
      params.facultyId = facultyId.value
    }
    return params
  }

  function persist() {
    if (accessToken.value) localStorage.setItem(ACCESS_KEY, accessToken.value)
    else localStorage.removeItem(ACCESS_KEY)

    if (refreshToken.value) localStorage.setItem(REFRESH_KEY, refreshToken.value)
    else localStorage.removeItem(REFRESH_KEY)

    if (user.value) localStorage.setItem(USER_KEY, JSON.stringify(user.value))
    else localStorage.removeItem(USER_KEY)
  }

  function clearRefreshTimer() {
    if (refreshTimer) {
      clearTimeout(refreshTimer)
      refreshTimer = null
    }
  }

  async function redirectToLogin(redirectPath?: string) {
    const current = router.currentRoute.value
    if (current.name === 'Signin') return
    await router.push({
      name: 'Signin',
      query: { redirect: redirectPath || current.fullPath },
    })
  }

  function scheduleTokenRefresh() {
    clearRefreshTimer()
    if (!accessToken.value || !refreshToken.value) return

    const waitMs = msUntilRefresh(accessToken.value, 60_000)
    if (waitMs == null) return

    refreshTimer = setTimeout(async () => {
      const token = await refreshAccessToken()
      if (token) {
        scheduleTokenRefresh()
      } else {
        await redirectToLogin()
      }
    }, waitMs)
  }

  function startSessionWatch() {
    stopSessionWatch()
    scheduleTokenRefresh()

    visibilityHandler = () => {
      if (document.visibilityState !== 'visible') return
      void ensureValidSession()
    }
    document.addEventListener('visibilitychange', visibilityHandler)

    // Faol foydalanuvchi sessiyasini yangilab turish — ishlayotganda chiqarib yubormaslik
    activityHandler = () => {
      if (!accessToken.value || !refreshToken.value) return
      const now = Date.now()
      if (now - lastActivityRefreshAt < 60_000) return
      lastActivityRefreshAt = now
      if (isAccessTokenExpired(accessToken.value, 120_000)) {
        void refreshAccessToken().then((token) => {
          if (token) scheduleTokenRefresh()
        })
      } else {
        scheduleTokenRefresh()
      }
    }
    window.addEventListener('pointerdown', activityHandler, { passive: true })
    window.addEventListener('keydown', activityHandler, { passive: true })
  }

  function stopSessionWatch() {
    clearRefreshTimer()
    if (visibilityHandler) {
      document.removeEventListener('visibilitychange', visibilityHandler)
      visibilityHandler = null
    }
    if (activityHandler) {
      window.removeEventListener('pointerdown', activityHandler)
      window.removeEventListener('keydown', activityHandler)
      activityHandler = null
    }
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
    startSessionWatch()
  }

  function clearSession() {
    stopSessionWatch()
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
    if (refreshInFlight) return refreshInFlight

    refreshInFlight = (async () => {
      try {
        const { data } = await authApi.refresh(refreshToken.value!)
        accessToken.value = data.accessToken
        if (data.refreshToken) refreshToken.value = data.refreshToken
        if (data.user) user.value = data.user
        persist()
        scheduleTokenRefresh()
        return data.accessToken
      } catch {
        clearSession()
        return null
      } finally {
        refreshInFlight = null
      }
    })()

    return refreshInFlight
  }

  /** Access muddati tugagan bo'lsa refresh qiladi; imkonsiz bo'lsa login'ga yuboradi. */
  async function ensureValidSession(): Promise<boolean> {
    if (!accessToken.value && !refreshToken.value) {
      return false
    }

    if (accessToken.value && !isAccessTokenExpired(accessToken.value, 30_000)) {
      scheduleTokenRefresh()
      return true
    }

    const token = await refreshAccessToken()
    if (token) return true

    await redirectToLogin()
    return false
  }

  async function logout() {
    try {
      if (accessToken.value) await authApi.logout()
    } catch {
      // ignore network errors on logout
    } finally {
      clearSession()
      if (router.currentRoute.value.name !== 'Signin') {
        await router.push({ name: 'Signin' })
      }
    }
  }

  async function fetchProfile() {
    const { data } = await authApi.me()
    user.value = data
    persist()
    return data
  }

  async function updateProfile(payload: Parameters<typeof authApi.updateProfile>[0]) {
    const { data } = await authApi.updateProfile(payload)
    user.value = data
    persist()
    return data
  }

  return {
    accessToken,
    refreshToken,
    user,
    loading,
    isAuthenticated,
    displayName,
    isSuperAdmin,
    isAdmin,
    isDekan,
    isKafedra,
    hasFullAccess,
    facultyId,
    departmentId,
    hasRole,
    catalogScopeParams,
    login,
    logout,
    refreshAccessToken,
    clearSession,
    setSession,
    ensureValidSession,
    startSessionWatch,
    stopSessionWatch,
    redirectToLogin,
    fetchProfile,
    updateProfile,
  }
})
