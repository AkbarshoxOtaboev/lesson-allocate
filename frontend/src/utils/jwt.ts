/** JWT access token muddatini o'qish (ms). */
export function getAccessTokenExpiryMs(token: string | null | undefined): number | null {
  if (!token) return null
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null
    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(base64.length + ((4 - (base64.length % 4)) % 4), '=')
    const payload = JSON.parse(atob(padded)) as { exp?: number }
    if (typeof payload.exp !== 'number') return null
    return payload.exp * 1000
  } catch {
    return null
  }
}

export function isAccessTokenExpired(token: string | null | undefined, skewMs = 5000): boolean {
  const exp = getAccessTokenExpiryMs(token)
  if (exp == null) return true
  return Date.now() >= exp - skewMs
}

/** Access token tugashiga qancha vaqt qolganini hisoblab, refresh vaqtini belgilaydi. */
export function msUntilRefresh(token: string | null | undefined, leadMs = 60_000): number | null {
  const exp = getAccessTokenExpiryMs(token)
  if (exp == null) return null
  return Math.max(0, exp - Date.now() - leadMs)
}
