import { useEffect, useRef, useState } from 'react'
import { getJson, postJson, putJson, type AdminVisit, type MealSlot, type Senior } from './api'

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

const blankMeals = () => SLOTS.map((s) => ({ dishName: '', storageInfo: s.storage }))
const MAX_PICTURE_BYTES = 5 * 1024 * 1024
const ALLOWED_PICTURE_TYPES = ['image/jpeg', 'image/png', 'image/webp']

/** Registers a new visit, or edits [visit] and calls [onSaved] when given one. */
export function VisitForm({
  adminName,
  visit,
  onSaved,
}: {
  adminName: string
  visit?: AdminVisit
  onSaved?: () => void
}) {
  const now = new Date()
  const [seniors, setSeniors] = useState<Senior[]>([])
  const [dishNames, setDishNames] = useState<string[]>([])
  const [seniorId, setSeniorId] = useState(visit?.seniorId ?? '')
  const [scheduledAt, setScheduledAt] = useState(localValue(visit ? new Date(visit.scheduledAt) : now))
  const [completed, setCompleted] = useState(visit?.completed ?? true)
  const [attention, setAttention] = useState(visit?.check === 'ATTENTION')
  const [internalNote, setInternalNote] = useState(visit?.internalNote ?? '')
  const [familyMessage, setFamilyMessage] = useState(visit?.familyMessage ?? '')
  const [momentOfWeek, setMomentOfWeek] = useState(visit?.momentOfWeek ?? '')
  const [picture, setPicture] = useState(visit?.picture ?? null)
  const [readingPicture, setReadingPicture] = useState(false)
  const pictureFileInput = useRef<HTMLInputElement>(null)
  const pictureCameraInput = useRef<HTMLInputElement>(null)
  const [meals, setMeals] = useState(() =>
    visit
      ? SLOTS.map((s) => {
          const meal = visit.meals.find((m) => m.slot === s.slot)
          return { dishName: meal?.dishName ?? '', storageInfo: meal?.storageInfo ?? s.storage }
        })
      : blankMeals(),
  )
  const [nextVisitAt, setNextVisitAt] = useState(
    visit ? (visit.nextVisitAt ? localValue(new Date(visit.nextVisitAt)) : '') : localValue(inSevenDays(now)),
  )
  const [error, setError] = useState<string | null>(null)
  const [publishedFor, setPublishedFor] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    getJson<Senior[]>('/api/admin/seniors')
      .then((list) => {
        setSeniors(list)
        if (list.length === 1 && !visit) setSeniorId(list[0].id)
      })
      .catch((e: Error) => setError(e.message))
    getJson<string[]>('/api/admin/dish-names').then(setDishNames).catch(() => setDishNames([]))
  }, [visit])

  const senior = seniors.find((s) => s.id === seniorId)

  const submit = (event: React.FormEvent) => {
    event.preventDefault()
    setBusy(true)
    setError(null)
    const body = {
      seniorId,
      scheduledAt: new Date(scheduledAt).toISOString(),
      completed,
      check: attention ? 'ATTENTION' : 'ALL_AS_USUAL',
      familyMessage,
      momentOfWeek: momentOfWeek || null,
      internalNote: attention ? internalNote : null,
      meals: SLOTS.map((s, i) => ({ slot: s.slot, ...meals[i] })),
      nextVisitAt: nextVisitAt ? new Date(nextVisitAt).toISOString() : null,
      picture,
    }
    ;(visit ? putJson(`/api/admin/visits/${visit.id}`, body) : postJson('/api/admin/visits', body))
      .then(() => {
        if (onSaved) return onSaved()
        setPublishedFor(senior?.preferredName ?? null)
        setFamilyMessage('')
        setMomentOfWeek('')
        setInternalNote('')
        setAttention(false)
        setMeals(blankMeals())
        setPicture(null)
        if (pictureFileInput.current) pictureFileInput.current.value = ''
        if (pictureCameraInput.current) pictureCameraInput.current.value = ''
      })
      .catch((e: Error) => setError(e.message))
      .finally(() => setBusy(false))
  }

  const readPicture = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0]
    if (!file) return
    setError(null)
    if (!ALLOWED_PICTURE_TYPES.includes(file.type)) {
      setError('Kies een JPEG-, PNG- of WebP-afbeelding.')
      event.target.value = ''
      return
    }
    if (file.size > MAX_PICTURE_BYTES) {
      setError('De foto mag maximaal 5 MB groot zijn.')
      event.target.value = ''
      return
    }
    setReadingPicture(true)
    const reader = new FileReader()
    reader.onload = () => {
      if (typeof reader.result === 'string') setPicture(reader.result)
      else setError('De foto kon niet worden gelezen.')
      setReadingPicture(false)
      event.target.value = ''
    }
    reader.onerror = () => {
      setError('De foto kon niet worden gelezen.')
      setReadingPicture(false)
      event.target.value = ''
    }
    reader.readAsDataURL(file)
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
      <h1>{visit ? 'Bezoek bewerken' : 'Bezoek registreren'}</h1>

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
        Update voor de familie <span className="optional">— verplicht</span>
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

      <label>
        Foto <span className="optional">— optioneel, JPEG/PNG/WebP (max. 5 MB)</span>
        <span className="picture-actions">
          <button
            className="linkish"
            type="button"
            disabled={readingPicture}
            onClick={() => pictureFileInput.current?.click()}
          >
            Kies bestand
          </button>
          <button
            className="linkish"
            type="button"
            disabled={readingPicture}
            onClick={() => pictureCameraInput.current?.click()}
          >
            Neem foto
          </button>
        </span>
        <input
          ref={pictureFileInput}
          className="picture-input"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          onChange={readPicture}
          tabIndex={-1}
          aria-hidden="true"
        />
        <input
          ref={pictureCameraInput}
          className="picture-input"
          type="file"
          accept="image/jpeg,image/png,image/webp"
          capture="environment"
          onChange={readPicture}
          tabIndex={-1}
          aria-hidden="true"
        />
      </label>
      {picture && (
        <div className="visit-picture-preview">
          <img src={picture} alt="Voorbeeld van de bezoekfoto" />
          <button
            className="linkish"
            type="button"
            onClick={() => {
              setPicture(null)
              if (pictureFileInput.current) pictureFileInput.current.value = ''
              if (pictureCameraInput.current) pictureCameraInput.current.value = ''
            }}
          >
            Foto verwijderen
          </button>
        </div>
      )}

      <fieldset className="meal-section">
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
      <button className="btn publish" type="submit" disabled={busy || readingPicture}>
        {visit ? 'Opslaan' : 'Publiceren'}
      </button>
      {onSaved && (
        <button className="linkish" type="button" onClick={onSaved}>
          Annuleren
        </button>
      )}
    </form>
  )
}
