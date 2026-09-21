export type Role = 'ADMIN' | 'FAMILY'

export type Session = { displayName: string; role: Role }

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
  } | null
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

export async function postJson<T>(url: string, body: unknown): Promise<T> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...csrfHeader() },
    body: JSON.stringify(body),
  })
  if (!response.ok) throw await failure(response)
  return response.json()
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
