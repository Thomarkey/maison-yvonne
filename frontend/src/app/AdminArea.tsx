import { useEffect, useState } from 'react'
import { getJson, postJson, putJson, type Account, type AdminVisit, type Role, type Senior } from './api'
import { VisitForm } from './VisitForm'

const TABS = [
  { key: 'mine', label: 'Mijn bezoeken' },
  { key: 'seniors', label: 'Seniors' },
  { key: 'accounts', label: 'Accounts' },
] as const

type Tab = (typeof TABS)[number]['key']

const dateFormat = new Intl.DateTimeFormat('nl-BE', {
  weekday: 'long',
  day: 'numeric',
  month: 'long',
  hour: '2-digit',
  minute: '2-digit',
})

export function AdminArea({ adminName }: { adminName: string }) {
  const [tab, setTab] = useState<Tab>('mine')

  return (
    <>
      <nav className="tabs" role="tablist">
        {TABS.map((t) => (
          <button
            key={t.key}
            type="button"
            role="tab"
            aria-selected={tab === t.key}
            onClick={() => setTab(t.key)}
          >
            {t.label}
          </button>
        ))}
      </nav>
      {tab === 'mine' && <MyVisits adminName={adminName} />}
      {tab === 'seniors' && <SeniorsOverview adminName={adminName} />}
      {tab === 'accounts' && <AccountsOverview />}
    </>
  )
}

/** Tabs mount on open, so every visit to a tab fetches fresh data; [reload] refetches after an edit. */
function useAdminData<T>(url: string) {
  const [data, setData] = useState<T | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [version, setVersion] = useState(0)
  useEffect(() => {
    getJson<T>(url)
      .then(setData)
      .catch((e: Error) => setError(e.message))
  }, [url, version])
  return { data, error, reload: () => setVersion((v) => v + 1) }
}

function MyVisits({ adminName }: { adminName: string }) {
  const visits = useAdminData<AdminVisit[]>('/api/admin/visits/mine')
  const seniors = useAdminData<Senior[]>('/api/admin/seniors')
  // remounting the <details> closes it again once the new visit is saved
  const [added, setAdded] = useState(0)

  if (visits.error) return <p className="error">{visits.error}</p>
  if (!visits.data) return null

  const name = (seniorId: string) =>
    seniors.data?.find((s) => s.id === seniorId)?.preferredName ?? '—'
  return (
    <>
      <details className="new-item" key={added}>
        <summary className="btn">+ Nieuw bezoek</summary>
        <VisitForm
          adminName={adminName}
          onSaved={() => {
            setAdded((n) => n + 1)
            visits.reload()
          }}
        />
      </details>
      <VisitList
        visits={visits.data}
        seniorName={name}
        editable={new Set(visits.data.map((v) => v.id))}
        adminName={adminName}
        onChanged={visits.reload}
      />
    </>
  )
}

function SeniorsOverview({ adminName }: { adminName: string }) {
  const visits = useAdminData<AdminVisit[]>('/api/admin/visits')
  // only your own visits can be edited, from any tab
  const mine = useAdminData<AdminVisit[]>('/api/admin/visits/mine')
  const seniors = useAdminData<Senior[]>('/api/admin/seniors')
  const [editing, setEditing] = useState<string | null>(null)

  const error = visits.error ?? seniors.error
  if (error) return <p className="error">{error}</p>
  if (!visits.data || !seniors.data) return null

  const editable = new Set(mine.data?.map((v) => v.id))
  const changed = () => {
    visits.reload()
    mine.reload()
  }

  return (
    <>
      <details className="new-item">
        <summary className="btn">+ Nieuwe senior</summary>
        <SeniorForm onSaved={seniors.reload} />
      </details>
      {seniors.data.map((senior) => {
        const theirs = visits.data?.filter((v) => v.seniorId === senior.id) ?? []
        return (
          <details className="card senior" key={senior.id}>
            <summary>
              {senior.preferredName} <span className="optional">— {theirs.length} bezoeken</span>
            </summary>
            {editing === senior.id ? (
              <SeniorForm
                senior={senior}
                onSaved={() => {
                  setEditing(null)
                  seniors.reload()
                }}
                onCancel={() => setEditing(null)}
              />
            ) : (
              <p>
                {senior.fullName}{' '}
                <button type="button" className="linkish" onClick={() => setEditing(senior.id)}>
                  Gegevens bewerken
                </button>
              </p>
            )}
            <VisitList visits={theirs} editable={editable} adminName={adminName} onChanged={changed} showVisitor />
          </details>
        )
      })}
    </>
  )
}

function AccountsOverview() {
  const accounts = useAdminData<Account[]>('/api/admin/accounts')
  const seniors = useAdminData<Senior[]>('/api/admin/seniors')

  if (accounts.error) return <p className="error">{accounts.error}</p>
  if (!accounts.data) return null

  const seniorName = (id: string) => seniors.data?.find((s) => s.id === id)?.fullName ?? '—'
  // e.g. "dochter van Louisa Vermeulen"
  const relation = (account: Account) =>
    account.links.map((l) => `${l.relationLabel ?? 'familie'} van ${seniorName(l.seniorId)}`).join(', ')

  return (
    <>
      <details className="new-item">
        <summary className="btn">+ Nieuw account</summary>
        <AccountForm onSaved={accounts.reload} />
      </details>
      <AccountList title="Admins" accounts={accounts.data.filter((a) => a.role === 'ADMIN')} />
      <AccountList
        title="Familie"
        accounts={accounts.data.filter((a) => a.role === 'FAMILY')}
        detail={relation}
      />
    </>
  )
}

function AccountList({
  title,
  accounts,
  detail,
}: {
  title: string
  accounts: Account[]
  detail?: (account: Account) => string
}) {
  const [resetting, setResetting] = useState<string | null>(null)

  return (
    <section className="card">
      <p className="eyebrow">{title}</p>
      {accounts.length === 0 ? (
        <p>Nog geen accounts.</p>
      ) : (
        <ul className="meals">
          {accounts.map((a) => (
            <li key={a.id}>
              <span>
                <strong>{a.displayName}</strong>
                <em>
                  {a.email}
                  {detail?.(a) && ` · ${detail(a)}`}
                </em>{' '}
                {resetting === a.id ? (
                  <ResetPasswordForm account={a} onDone={() => setResetting(null)} />
                ) : (
                  <button type="button" className="linkish" onClick={() => setResetting(a.id)}>
                    Wachtwoord resetten
                  </button>
                )}
              </span>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

/** Sets a new starting password for someone who forgot theirs; pass it on to them yourself. */
function ResetPasswordForm({ account, onDone }: { account: Account; onDone: () => void }) {
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const submit = (event: React.FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    putJson(`/api/admin/accounts/${account.id}/password`, { newPassword })
      .then(onDone)
      .catch((e: Error) => setError(e.message))
      .finally(() => setBusy(false))
  }

  return (
    <form onSubmit={submit}>
      <label>
        Nieuw startwachtwoord voor {account.displayName} <span className="optional">— minstens 8 tekens</span>
        <input
          type="text"
          autoComplete="off"
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

/** Hands out an account with a starting password; its owner changes it after logging in. */
function AccountForm({ onSaved }: { onSaved: () => void }) {
  const seniors = useAdminData<Senior[]>('/api/admin/seniors')
  const empty = { email: '', displayName: '', role: 'FAMILY' as Role, password: '', seniorId: '', relationLabel: '' }
  const [form, setForm] = useState(empty)
  const [error, setError] = useState<string | null>(null)
  const [created, setCreated] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const set = (field: keyof typeof empty) => (e: { target: { value: string } }) =>
    setForm({ ...form, [field]: e.target.value })
  const family = form.role === 'FAMILY'

  const submit = (event: React.FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    setCreated(null)
    postJson('/api/admin/accounts', { ...form, seniorId: family ? form.seniorId : null })
      .then(() => {
        setCreated(form.email)
        setForm(empty)
        onSaved()
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setBusy(false))
  }

  return (
    <form onSubmit={submit}>
      <label>
        E-mailadres
        <input type="email" value={form.email} onChange={set('email')} required />
      </label>
      <label>
        Naam
        <input value={form.displayName} onChange={set('displayName')} required />
      </label>
      <label>
        Rol
        <select value={form.role} onChange={set('role')}>
          <option value="FAMILY">Familie</option>
          <option value="ADMIN">Admin</option>
        </select>
      </label>
      {family && (
        <>
          <label>
            Volgt senior
            <select value={form.seniorId} onChange={set('seniorId')} required>
              <option value="">— kies —</option>
              {seniors.data?.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.fullName}
                </option>
              ))}
            </select>
          </label>
          <label>
            Relatie <span className="optional">— bv. dochter</span>
            <input value={form.relationLabel} onChange={set('relationLabel')} />
          </label>
        </>
      )}
      <label>
        Startwachtwoord <span className="optional">— minstens 8 tekens, geef het zelf door</span>
        <input type="text" autoComplete="off" value={form.password} onChange={set('password')} minLength={8} required />
      </label>
      {error && <p className="error">{error}</p>}
      {created && <p>Account voor {created} aangemaakt.</p>}
      <button className="btn" type="submit" disabled={busy}>
        Account aanmaken
      </button>
    </form>
  )
}

/** Creates a senior, or edits [senior] when given one. */
function SeniorForm({
  senior,
  onSaved,
  onCancel,
}: {
  senior?: Senior
  onSaved: () => void
  onCancel?: () => void
}) {
  const [preferredName, setPreferredName] = useState(senior?.preferredName ?? '')
  const [fullName, setFullName] = useState(senior?.fullName ?? '')
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  const submit = (event: React.FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    const body = { preferredName, fullName }
    ;(senior ? putJson(`/api/admin/seniors/${senior.id}`, body) : postJson('/api/admin/seniors', body))
      .then(() => {
        if (!senior) {
          setPreferredName('')
          setFullName('')
        }
        onSaved()
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setBusy(false))
  }

  return (
    <form onSubmit={submit}>
      <label>
        Roepnaam <span className="optional">— leeg = voornaam</span>
        <input value={preferredName} onChange={(e) => setPreferredName(e.target.value)} />
      </label>
      <label>
        Volledige naam
        <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
      </label>
      {error && <p className="error">{error}</p>}
      <button className="btn" type="submit" disabled={busy}>
        {senior ? 'Opslaan' : 'Senior toevoegen'}
      </button>
      {onCancel && (
        <button className="linkish" type="button" onClick={onCancel}>
          Annuleren
        </button>
      )}
    </form>
  )
}

/**
 * Upcoming = visits scheduled after now, plus the "volgend bezoek" each senior's latest visit
 * planned. [visits] arrive newest first. Only visits in [editable] get an edit button.
 */
function VisitList({
  visits,
  seniorName,
  editable,
  adminName,
  onChanged,
  showVisitor = false,
}: {
  visits: AdminVisit[]
  seniorName?: (id: string) => string
  editable: Set<string>
  adminName: string
  onChanged: () => void
  /** Show which admin did each visit — for overviews that mix visits of several admins. */
  showVisitor?: boolean
}) {
  const [editing, setEditing] = useState<AdminVisit | null>(null)

  if (editing) {
    return (
      <VisitForm
        adminName={adminName}
        visit={editing}
        onSaved={() => {
          setEditing(null)
          onChanged()
        }}
      />
    )
  }

  const now = Date.now()
  const past = visits.filter((v) => Date.parse(v.scheduledAt) <= now)
  const latestPerSenior = visits.filter((v, i) => visits.findIndex((w) => w.seniorId === v.seniorId) === i)
  const upcoming = [
    ...visits.filter((v) => Date.parse(v.scheduledAt) > now).map((v) => ({ visit: v, at: v.scheduledAt })),
    ...latestPerSenior
      .filter((v) => v.nextVisitAt && Date.parse(v.nextVisitAt) > now)
      .map((v) => ({ visit: v, at: v.nextVisitAt as string })),
  ].sort((a, b) => Date.parse(a.at) - Date.parse(b.at))

  const visitor = (visit: AdminVisit) =>
    showVisitor && visit.registeredBy && <span className="optional"> · {visit.registeredBy}</span>

  const editButton = (visit: AdminVisit) =>
    editable.has(visit.id) && (
      <button type="button" className="linkish" onClick={() => setEditing(visit)}>
        Bewerken
      </button>
    )

  return (
    <>
      <section className="card">
        <p className="eyebrow">Komende bezoeken</p>
        {upcoming.length === 0 ? (
          <p>Geen bezoeken gepland.</p>
        ) : (
          <ul className="meals">
            {upcoming.map((u) => (
              <li key={`${u.visit.id}-${u.at}`}>
                <span>
                  {seniorName && <strong>{seniorName(u.visit.seniorId)}</strong>}
                  <em>
                    {dateFormat.format(new Date(u.at))}
                    {visitor(u.visit)}
                  </em>{' '}
                  {editButton(u.visit)}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>

      <section className="card">
        <p className="eyebrow">Vorige bezoeken</p>
        {past.length === 0 ? (
          <p>Nog geen bezoeken geregistreerd.</p>
        ) : (
          <ul className="meals">
            {past.map((v) => (
              <li key={v.id}>
                <span>
                  <strong>
                    {seniorName && `${seniorName(v.seniorId)} `}
                    {v.completed ? '✓' : '— nog niet voltooid'}
                    {v.check === 'ATTENTION' && ' · aandachtspunt'}
                  </strong>
                  <em>
                    {dateFormat.format(new Date(v.scheduledAt))}
                    {visitor(v)}
                  </em>{' '}
                  {editButton(v)}
                  <p className="message">{v.familyMessage}</p>
                  {v.internalNote && <em>Interne notitie: {v.internalNote}</em>}
                </span>
              </li>
            ))}
          </ul>
        )}
      </section>
    </>
  )
}
