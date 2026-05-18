export interface AuthTokenPayload {
  accessToken: string
  refreshToken?: string
}

export function getAuthPayload(res: any): AuthTokenPayload | null {
  if (res?.code && res.code !== 0) {
    return null
  }

  const payload = res?.data ?? res
  const accessToken = payload?.accessToken ?? payload?.access_token ?? payload?.token

  if (!accessToken) {
    return null
  }

  return {
    accessToken,
    refreshToken: payload?.refreshToken ?? payload?.refresh_token
  }
}

export function saveAuthSession(payload: AuthTokenPayload, mobile: string) {
  localStorage.setItem('token', payload.accessToken)
  localStorage.setItem('refreshToken', payload.refreshToken || '')
  localStorage.setItem('username', mobile)
}

export function clearAuthSession() {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('username')
  localStorage.removeItem('user')
}

export function getErrorMessage(err: any, fallback: string) {
  return err?.response?.data?.msg || err?.response?.data?.message || err?.message || fallback
}

export function getApiResultMessage(res: any, fallback: string) {
  return res?.msg || res?.message || fallback
}
