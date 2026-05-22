import { useAuthStore } from '@/stores/auth'

export interface LoginResponse {
  token: string
}

export interface OpeningResponse {
  id: number
  name: string
  pgn: string
  initialFen: string | null
  notes: string | null
  createdAt: string
}

export interface OpeningRequest {
  name: string
  pgn: string
  initialFen?: string | null
  notes?: string | null
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
  return authedFetch('/api/hello').then((r) => r.text())
}

export async function listOpenings(): Promise<OpeningResponse[]> {
  return authedFetch('/api/openings').then((r) => r.json())
}

export async function getOpening(id: number): Promise<OpeningResponse> {
  return authedFetch(`/api/openings/${id}`).then((r) => r.json())
}

export async function createOpening(req: OpeningRequest): Promise<OpeningResponse> {
  return authedFetch('/api/openings', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  }).then((r) => r.json())
}

export async function updateOpening(id: number, req: OpeningRequest): Promise<OpeningResponse> {
  return authedFetch(`/api/openings/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(req),
  }).then((r) => r.json())
}

export async function deleteOpening(id: number): Promise<void> {
  await authedFetch(`/api/openings/${id}`, { method: 'DELETE' })
}

async function authedFetch(url: string, init: RequestInit = {}): Promise<Response> {
  const auth = useAuthStore()
  const headers = new Headers(init.headers)
  if (auth.token) {
    headers.set('Authorization', `Bearer ${auth.token}`)
  }
  const res = await fetch(url, { ...init, headers })
  if (res.status === 401) {
    auth.clearToken()
    throw new Error('Session expired — please log in again')
  }
  if (!res.ok) {
    throw new Error(`Request failed (${res.status})`)
  }
  return res
}
