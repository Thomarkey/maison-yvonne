import { useEffect, useRef, useState } from 'react'
import { currentSession, login, logout, postJson, type Session } from './api'
import { AdminArea } from './AdminArea'
import { FamilyDashboard } from './FamilyDashboard'
import './app.css'

export function AppShell() {
  const [session, setSession] = useState<Session | null>(null)
  const [loading, setLoading] = useState(true)
  const [changingPassword, setChangingPassword] = useState(false)
  const profileMenu = useRef<HTMLDetailsElement>(null)

  useEffect(() => {
    currentSession()
      .then(setSession)
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    const closeOnOutsideClick = (event: PointerEvent) => {
      if (event.target instanceof Node && !profileMenu.current?.contains(event.target)) {
        profileMenu.current?.removeAttribute('open')
      }
    }
    document.addEventListener('pointerdown', closeOnOutsideClick)
    return () => document.removeEventListener('pointerdown', closeOnOutsideClick)
  }, [])

  if (loading) return <main className="app" />

  return (
    <main className="app">
      <header className="app-bar">
        <span className="app-mark">MAISON YVONNE</span>
        {session && (
          <details className="profile-menu" ref={profileMenu}>
            <summary aria-label={`Profiel van ${session.displayName}`}>
              <span className="profile-summary-name">{session.displayName}</span>
              <span className="profile-menu-chevron" aria-hidden="true" />
            </summary>
            <div className="profile-panel">
              <span className="profile-email">{session.email}</span>
              <span className="profile-role">{session.role === 'ADMIN' ? 'Admin' : 'Familie'}</span>
              <button
                type="button"
                onClick={() => {
                  setChangingPassword(true)
                  profileMenu.current?.removeAttribute('open')
                }}
              >
                Wachtwoord wijzigen
              </button>
              <button
                type="button"
                onClick={() => logout().then(() => {
                  setChangingPassword(false)
                  setSession(null)
                })}
              >
                Afmelden
              </button>
            </div>
          </details>
        )}
      </header>
      {!session && <LoginForm onLoggedIn={setSession} />}
      {session && changingPassword && <PasswordForm onDone={() => setChangingPassword(false)} />}
      {session?.role === 'ADMIN' && <AdminArea adminName={session.displayName} />}
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

function PasswordForm({ onDone }: { onDone: () => void }) {
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const submit = (event: React.FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    postJson('/api/me/password', { currentPassword, newPassword })
      .then(onDone)
      .catch((e: Error) => setError(e.message))
      .finally(() => setBusy(false))
  }

  return (
    <form className="card login" onSubmit={submit}>
      <h1>Wachtwoord wijzigen</h1>
      <label>
        Huidig wachtwoord
        <input
          type="password"
          autoComplete="current-password"
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
          required
        />
      </label>
      <label>
        Nieuw wachtwoord <span className="optional">— minstens 8 tekens</span>
        <input
          type="password"
          autoComplete="new-password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          minLength={8}
          required
        />
      </label>
      {error && <p className="error">{error}</p>}
      <button className="btn" type="submit" disabled={busy}>
        Opslaan
      </button>
      <button className="linkish" type="button" onClick={onDone}>
        Annuleren
      </button>
    </form>
  )
}
