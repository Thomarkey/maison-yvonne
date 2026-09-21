import { useEffect, useState } from 'react'
import { getJson, postJson, type MealSlot } from './api'

type Senior = { id: string; preferredName: string }

const SLOTS: { slot: MealSlot; number: string; label: string; storage: string }[] = [
  { slot: 'FIRST', number: '1', label: 'Eerst van genieten', storage: 'Vandaag of morgen eten' },
  { slot: 'SECOND', number: '2', label: 'Daarna', storage: 'Binnen 3 dagen eten' },
  { slot: 'LAST', number: '3', label: 'Bewaar mij voor het laatst', storage: 'Houdt 5 dagen' },
]

/** <input type="datetime-local"> wants local wall-clock time, not UTC. */
function localValue(date: Date) {
  return new Date(date.getTime() - date.getTimezoneOffset() * 60_000).toISOString().slice(0, 16)
}

const inSevenDays = (from: Date) => new Date(from.getTime() + 7 * 24 * 60 * 60 * 1000)

export function VisitForm({ adminName }: { adminName: string }) {
  const now = new Date()
  const [seniors, setSeniors] = useState<Senior[]>([])
  const [dishNames, setDishNames] = useState<string[]>([])
  const [seniorId, setSeniorId] = useState('')
  const [scheduledAt, setScheduledAt] = useState(localValue(now))
  const [completed, setCompleted] = useState(true)
  const [attention, setAttention] = useState(false)
  const [internalNote, setInternalNote] = useState('')
  const [familyMessage, setFamilyMessage] = useState('')
  const [momentOfWeek, setMomentOfWeek] = useState('')
  const [meals, setMeals] = useState(SLOTS.map((s) => ({ dishName: '', storageInfo: s.storage })))
  const [nextVisitAt, setNextVisitAt] = useState(localValue(inSevenDays(now)))
  const [error, setError] = useState<string | null>(null)
  const [publishedFor, setPublishedFor] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    getJson<Senior[]>('/api/admin/seniors')
      .then((list) => {
        setSeniors(list)
        if (list.length === 1) setSeniorId(list[0].id)
      })
      .catch((e: Error) => setError(e.message))
    getJson<string[]>('/api/admin/dish-names').then(setDishNames).catch(() => setDishNames([]))
  }, [])

  const senior = seniors.find((s) => s.id === seniorId)

  const submit = (event: React.FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    postJson('/api/admin/visits', {
      seniorId,
      scheduledAt: new Date(scheduledAt).toISOString(),
      completed,
      check: attention ? 'ATTENTION' : 'ALL_AS_USUAL',
      familyMessage,
      momentOfWeek: momentOfWeek || null,
      internalNote: attention ? internalNote : null,
      meals: SLOTS.map((s, i) => ({ slot: s.slot, ...meals[i] })),
      nextVisitAt: nextVisitAt ? new Date(nextVisitAt).toISOString() : null,
    })
      .then(() => {
        setPublishedFor(senior?.preferredName ?? null)
        setFamilyMessage('')
        setMomentOfWeek('')
        setInternalNote('')
        setAttention(false)
        setMeals(SLOTS.map((s) => ({ dishName: '', storageInfo: s.storage })))
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setBusy(false))
  }

  if (publishedFor) {
    return (
      <section className="card published">
        <p className="tick">✓</p>
        <h1>Gepubliceerd.</h1>
        <p>De familie van {publishedFor} ziet de update nu.</p>
        <button className="btn" type="button" onClick={() => setPublishedFor(null)}>
          Nog een bezoek registreren
        </button>
      </section>
    )
  }

  return (
    <form className="visit-form" onSubmit={submit}>
      <p className="hello">Dag {adminName}</p>
      <h1>Bezoek registreren</h1>

      <label>
        Senior
        <select value={seniorId} onChange={(e) => setSeniorId(e.target.value)} required>
          <option value="">Kies…</option>
          {seniors.map((s) => (
            <option key={s.id} value={s.id}>
              {s.preferredName}
            </option>
          ))}
        </select>
      </label>

      <div className="row">
        <label>
          Datum en uur
          <input
            type="datetime-local"
            value={scheduledAt}
            onChange={(e) => setScheduledAt(e.target.value)}
            required
          />
        </label>
        <label className="check">
          <input type="checkbox" checked={completed} onChange={(e) => setCompleted(e.target.checked)} />
          Bezoek voltooid
        </label>
      </div>

      <fieldset>
        <legend>Yvonne Check</legend>
        <label className="check">
          <input type="radio" checked={!attention} onChange={() => setAttention(false)} />
          Alles zoals gewoonlijk
        </label>
        <label className="check">
          <input type="radio" checked={attention} onChange={() => setAttention(true)} />
          Aandachtspunt
        </label>
        {attention && (
          <label>
            Interne notitie — niet zichtbaar voor de familie
            <textarea
              rows={2}
              value={internalNote}
              onChange={(e) => setInternalNote(e.target.value)}
              required
            />
          </label>
        )}
      </fieldset>

      <label>
        Update voor de familie
        <textarea
          rows={4}
          value={familyMessage}
          onChange={(e) => setFamilyMessage(e.target.value)}
          placeholder={`Hoe ging het bezoek bij ${senior?.preferredName ?? 'de senior'}? Geen medische details.`}
          required
        />
      </label>

      <label>
        Moment van de week <span className="optional">— optioneel</span>
        <input value={momentOfWeek} onChange={(e) => setMomentOfWeek(e.target.value)} />
      </label>

      <fieldset>
        <legend>Maaltijden</legend>
        <datalist id="dish-names">
          {dishNames.map((name) => (
            <option key={name} value={name} />
          ))}
        </datalist>
        {SLOTS.map((s, i) => (
          <div className="meal" key={s.slot}>
            <p className="meal-head">
              <span className="meal-number">{s.number}</span>
              {s.label}
            </p>
            <input
              list="dish-names"
              value={meals[i].dishName}
              onChange={(e) =>
                setMeals(meals.map((m, j) => (j === i ? { ...m, dishName: e.target.value } : m)))
              }
              placeholder="Naam gerecht"
              required={completed}
            />
            <input
              value={meals[i].storageInfo}
              onChange={(e) =>
                setMeals(meals.map((m, j) => (j === i ? { ...m, storageInfo: e.target.value } : m)))
              }
              placeholder="Bewaar- of eetinformatie"
              required={completed}
            />
          </div>
        ))}
      </fieldset>

      <label>
        Volgend bezoek
        <input
          type="datetime-local"
          value={nextVisitAt}
          onChange={(e) => setNextVisitAt(e.target.value)}
        />
      </label>

      {error && <p className="error">{error}</p>}
      <button className="btn publish" type="submit" disabled={busy}>
        Publiceren
      </button>
    </form>
  )
}
