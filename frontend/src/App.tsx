import { useEffect, useState } from 'react'

type Senior = { id: string; name: string }
type Visit = { id: string; seniorId: string; scheduledAt: string; randomInfo: string | null }

async function send<T>(method: string, url: string, body?: unknown): Promise<T | null> {
  const res = await fetch(url, {
    method,
    headers: body ? { 'Content-Type': 'application/json' } : undefined,
    body: body ? JSON.stringify(body) : undefined,
  })
  // the advice answers 404/409 with a plain-text message — show that, not the status code
  if (!res.ok) throw new Error((await res.text()) || `${res.status} ${res.statusText}`)
  return res.status === 204 || !res.headers.get('content-type')?.includes('json')
    ? null
    : res.json()
}

export function App() {
  const [seniors, setSeniors] = useState<Senior[]>([])
  const [visits, setVisits] = useState<Visit[]>([])
  const [editing, setEditing] = useState<Senior | null>(null)
  const [editingVisit, setEditingVisit] = useState<Visit | null>(null)
  const [error, setError] = useState('')

  const run = (fn: () => Promise<void>) =>
    fn().then(() => setError(''), (e: unknown) => setError(e instanceof Error ? e.message : String(e)))

  useEffect(() => {
    run(async () => {
      const [loadedSeniors, loadedVisits] = await Promise.all([
        send<Senior[]>('GET', '/api/seniors'),
        send<Visit[]>('GET', '/api/visits'),
      ])
      setSeniors(loadedSeniors ?? [])
      setVisits(loadedVisits ?? [])
    })
  }, [])

  function createSenior(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = e.currentTarget
    const name = new FormData(form).get('name') as string
    run(async () => {
      const senior = await send<Senior>('POST', '/api/seniors', { name })
      if (senior) setSeniors([...seniors, senior])
      form.reset()
    })
  }

  function saveEdit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault()
    if (!editing) return
    const { id } = editing
    const name = new FormData(e.currentTarget).get('name') as string
    run(async () => {
      const senior = await send<Senior>('PUT', '/api/seniors', { seniorId: id, name })
      if (senior) setSeniors(seniors.map((s) => (s.id === id ? senior : s)))
      setEditing(null)
    })
  }

  function deleteSenior(id: string) {
    run(async () => {
      await send('DELETE', `/api/seniors/${id}`)
      setSeniors(seniors.filter((s) => s.id !== id))
      setVisits(visits.filter((v) => v.seniorId !== id))
      if (editing?.id === id) setEditing(null)
      if (editingVisit?.seniorId === id) setEditingVisit(null)
    })
  }

  function saveVisitEdit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault()
    if (!editingVisit) return
    const { id } = editingVisit
    const data = new FormData(e.currentTarget)
    run(async () => {
      const visit = await send<Visit>('PUT', '/api/visits', {
        visitId: id,
        date: new Date(data.get('date') as string).toISOString(),
        randomInfo: data.get('randomInfo') || null,
      })
      if (visit) setVisits(visits.map((v) => (v.id === id ? visit : v)))
      setEditingVisit(null)
    })
  }

  function deleteVisit(id: string) {
    run(async () => {
      await send('DELETE', `/api/visits/${id}`)
      setVisits(visits.filter((v) => v.id !== id))
      if (editingVisit?.id === id) setEditingVisit(null)
    })
  }

  function registerVisit(e: React.FormEvent<HTMLFormElement>) {
    e.preventDefault()
    const form = e.currentTarget
    const data = new FormData(form)
    run(async () => {
      const visit = await send<Visit>('POST', '/api/visits/register', {
        seniorId: data.get('seniorId'),
        date: new Date(data.get('date') as string).toISOString(),
        randomInfo: data.get('randomInfo') || null,
      })
      if (visit) setVisits([...visits, visit])
      form.reset()
    })
  }

  const nameOf = (id: string) => seniors.find((s) => s.id === id)?.name ?? id

  const forDateInput = (iso: string) => {
    const d = new Date(iso)
    return new Date(d.getTime() - d.getTimezoneOffset() * 60_000).toISOString().slice(0, 16)
  }

  return (
    <main>
      <h1>Maison Yvonne</h1>
      {error && <p className="error">{error}</p>}

      <section>
        <h2>Seniors</h2>
        <form onSubmit={createSenior}>
          <input name="name" placeholder="Name" required />
          <button>Create senior</button>
        </form>
        <ul>
          {seniors.map((s) => (
            <li key={s.id}>
              {editing?.id === s.id ? (
                <form onSubmit={saveEdit}>
                  <input name="name" defaultValue={s.name} required autoFocus />
                  <button>Save</button>
                  <button type="button" onClick={() => setEditing(null)}>
                    Cancel
                  </button>
                </form>
              ) : (
                <>
                  {s.name} <code>{s.id}</code>
                  <span className="actions">
                    <button onClick={() => setEditing(s)}>Rename</button>
                    <button onClick={() => deleteSenior(s.id)}>Delete</button>
                  </span>
                </>
              )}
            </li>
          ))}
          {seniors.length === 0 && <li className="empty">No seniors yet.</li>}
        </ul>
      </section>

      <section>
        <h2>Visits</h2>
        <form onSubmit={registerVisit}>
          <select name="seniorId" required defaultValue="">
            <option value="" disabled>
              Pick a senior…
            </option>
            {seniors.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
          <input name="date" type="datetime-local" required />
          <input name="randomInfo" placeholder="Note (optional)" />
          <button disabled={seniors.length === 0}>Register visit</button>
        </form>
        <ul>
          {visits.map((v) => (
            <li key={v.id}>
              {editingVisit?.id === v.id ? (
                <form onSubmit={saveVisitEdit}>
                  <input
                    name="date"
                    type="datetime-local"
                    defaultValue={forDateInput(v.scheduledAt)}
                    required
                    autoFocus
                  />
                  <input name="randomInfo" defaultValue={v.randomInfo ?? ''} placeholder="Note (optional)" />
                  <button>Save</button>
                  <button type="button" onClick={() => setEditingVisit(null)}>
                    Cancel
                  </button>
                </form>
              ) : (
                <>
                  {nameOf(v.seniorId)} — {new Date(v.scheduledAt).toLocaleString()}
                  {v.randomInfo && ` — ${v.randomInfo}`}
                  <span className="actions">
                    <button onClick={() => setEditingVisit(v)}>Edit</button>
                    <button onClick={() => deleteVisit(v.id)}>Delete</button>
                  </span>
                </>
              )}
            </li>
          ))}
          {visits.length === 0 && <li className="empty">No visits yet.</li>}
        </ul>
      </section>
    </main>
  )
}
