export type Role = 'ADMIN' | 'FAMILY'

export type Session = { displayName: string; email: string; role: Role }

export type MealSlot = 'FIRST' | 'SECOND' | 'LAST'

export type Meal = { slot: MealSlot; label: string; dishName: string; storageInfo: string }

export type FamilyUpdate = {
  senior: { id: string; preferredName: string }
  relationLabel: string | null
  lastVisit: {
    visitedAt: string
    completed: boolean
    check: 'ALL_AS_USUAL' | 'ATTENTION'
    familyMessage: string
    momentOfWeek: string | null
    meals: Meal[]
    nextVisitAt: string | null
    picture: string | null
  } | null
}

export type AdminVisit = {
  id: string
  seniorId: string
  scheduledAt: string
  completed: boolean
  check: 'ALL_AS_USUAL' | 'ATTENTION'
  familyMessage: string
  momentOfWeek: string | null
  internalNote: string | null
  meals: Meal[]
  nextVisitAt: string | null
  publishedAt: string | null
  picture: string | null
  registeredBy: string | null
}

// Spring's SPA CSRF setup refreshes this cookie on every response; echo it back as a header.
const CSRF_COOKIE = 'XSRF-TOKEN'
const CSRF_HEADER = 'X-XSRF-TOKEN'

function csrfHeader(): Record<string, string> {
  const cookie = document.cookie.split('; ').find((c) => c.startsWith(CSRF_COOKIE + '='))
  return cookie ? { [CSRF_HEADER]: decodeURIComponent(cookie.slice(CSRF_COOKIE.length + 1)) } : {}
}

async function failure(response: Response) {
  const problem = await response.json().catch(() => null)
  return new Error(problem?.detail ?? 'Er ging iets mis. Probeer opnieuw.')
}

export async function getJson<T>(url: string): Promise<T> {
  const response = await fetch(url)
  if (!response.ok) throw await failure(response)
  return response.json()
}

export type Senior = { id: string; preferredName: string; fullName: string }

export type Account = {
  id: string
  email: string
  displayName: string
  role: Role
  links: { seniorId: string; relationLabel: string | null }[]
}

export const postJson = <T>(url: string, body: unknown) => sendJson<T>('POST', url, body)

export const putJson = <T>(url: string, body: unknown) => sendJson<T>('PUT', url, body)

async function sendJson<T>(method: 'POST' | 'PUT', url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method,
    headers: { 'Content-Type': 'application/json', ...csrfHeader() },
    body: JSON.stringify(body),
  })
  if (!response.ok) throw await failure(response)
  return response.status === 204 ? (undefined as T) : response.json()
}

export async function login(email: string, password: string) {
  const response = await fetch('/api/auth/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded', ...csrfHeader() },
    body: new URLSearchParams({ username: email, password }),
  })
  if (!response.ok) throw new Error('E-mailadres of wachtwoord is niet juist.')
}

export async function logout() {
  await fetch('/api/auth/logout', { method: 'POST', headers: csrfHeader() })
}

export async function currentSession(): Promise<Session | null> {
  const response = await fetch('/api/me')
  return response.ok ? response.json() : null
}
