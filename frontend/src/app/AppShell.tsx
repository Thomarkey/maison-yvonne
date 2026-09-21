import { useEffect, useState } from 'react'
import { currentSession, login, logout, type Session } from './api'
import { FamilyDashboard } from './FamilyDashboard'
import { VisitForm } from './VisitForm'
import './app.css'

export function AppShell() {
  const [session, setSession] = useState<Session | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    currentSession()
      .then(setSession)
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <main className="app" />

  return (
    <main className="app">
      <header className="app-bar">
        <span className="app-mark">MAISON YVONNE</span>
        {session && (
          <button
            type="button"
            className="linkish"
            onClick={() => logout().then(() => setSession(null))}
          >
            Afmelden
          </button>
        )}
      </header>
      {!session && <LoginForm onLoggedIn={setSession} />}
      {session?.role === 'ADMIN' && <VisitForm adminName={session.displayName} />}
      {session?.role === 'FAMILY' && <FamilyDashboard />}
    </main>
  )
}

function LoginForm({ onLoggedIn }: { onLoggedIn: (session: Session) => void }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const submit = (event: React.FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    login(email, password)
      .then(currentSession)
      .then((session) => session && onLoggedIn(session))
      .catch((e: Error) => setError(e.message))
      .finally(() => setBusy(false))
  }

  return (
    <form className="card login" onSubmit={submit}>
      <h1>Aanmelden</h1>
      <label>
        E-mailadres
        <input
          type="email"
          autoComplete="username"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
      </label>
      <label>
        Wachtwoord
        <input
          type="password"
          autoComplete="current-password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
      </label>
      {error && <p className="error">{error}</p>}
      <button className="btn" type="submit" disabled={busy}>
        Aanmelden
      </button>
    </form>
  )
}
