import { useAuthStore } from '@/stores/auth'

export interface LoginResponse {
  token: string
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const res = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  })
  if (!res.ok) {
    throw new Error(res.status === 401 ? 'Invalid email or password' : `Login failed (${res.status})`)
  }
  return res.json()
}

export async function getHello(): Promise<string> {
  const auth = useAuthStore()
  const res = await fetch('/api/hello', {
    headers: auth.token ? { Authorization: `Bearer ${auth.token}` } : {},
  })
  if (res.status === 401) {
    auth.clearToken()
    throw new Error('Session expired — please log in again')
  }
  if (!res.ok) {
    throw new Error(`Request failed (${res.status})`)
  }
  return res.text()
}
