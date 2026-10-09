import { useEffect, useState } from 'react'
import { getJson, postJson, putJson, type AdminVisit, type Senior } from './api'
import { VisitForm } from './VisitForm'

const TABS = [
  { key: 'mine', label: 'Mijn bezoeken' },
  { key: 'seniors', label: 'Seniors' },
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
