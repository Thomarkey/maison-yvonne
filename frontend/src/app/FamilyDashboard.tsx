import { useEffect, useState } from 'react'
import { getJson, type FamilyUpdate } from './api'

const dayFormat = new Intl.DateTimeFormat('nl-BE', { weekday: 'long', day: 'numeric', month: 'long' })

const isToday = (date: Date) => date.toDateString() === new Date().toDateString()

const MEAL_NUMBERS = ['1', '2', '3']

export function FamilyDashboard() {
  const [updates, setUpdates] = useState<FamilyUpdate[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getJson<FamilyUpdate[]>('/api/family/dashboard')
      .then(setUpdates)
      .catch((e: Error) => setError(e.message))
  }, [])

  if (error) return <p className="error">{error}</p>
  if (!updates) return null

  return (
    <>
      {updates.map(({ senior, lastVisit }) => {
        if (!lastVisit) {
          return (
            <section className="card" key={senior.id}>
              <h1>{senior.preferredName}</h1>
              <p>Zodra Maison Yvonne langs is geweest, vind je de update hier.</p>
            </section>
          )
        }

        const visitedAt = new Date(lastVisit.visitedAt)
        const when = isToday(visitedAt) ? 'vandaag' : `op ${dayFormat.format(visitedAt)}`

        return (
          <article key={senior.id}>
            <section className={`visited ${lastVisit.completed ? '' : 'visited--missed'}`}>
              <p className="tick">{lastVisit.completed ? '✓' : '—'}</p>
              <h1>
                {lastVisit.completed
                  ? `Maison Yvonne was ${when} bij ${senior.preferredName}`
                  : `Het bezoek bij ${senior.preferredName} ging ${when} niet door`}
              </h1>
              <p className="date">{dayFormat.format(visitedAt)}</p>
            </section>

            <p className={`check ${lastVisit.check === 'ATTENTION' ? 'check--attention' : ''}`}>
              {lastVisit.check === 'ALL_AS_USUAL'
                ? 'Alles zoals gewoonlijk'
                : 'Aandachtspunt — we nemen contact op'}
            </p>

            <section className="card">
              <p className="message">{lastVisit.familyMessage}</p>
              {lastVisit.momentOfWeek && (
                <>
                  <p className="eyebrow">Moment van de week</p>
                  <p className="moment">{lastVisit.momentOfWeek}</p>
                </>
              )}
            </section>

            {lastVisit.meals.length > 0 && (
              <section className="card">
                <p className="eyebrow">Deze week voor {senior.preferredName}</p>
                <ul className="meals">
                  {lastVisit.meals.map((meal, i) => (
                    <li key={meal.slot}>
                      <span className="meal-number">{MEAL_NUMBERS[i]}</span>
                      <span>
                        <strong>{meal.dishName}</strong>
                        <em>{meal.storageInfo}</em>
                      </span>
                    </li>
                  ))}
                </ul>
              </section>
            )}

            {lastVisit.nextVisitAt && (
              <section className="card next">
                <p className="eyebrow">Volgend bezoek</p>
                <p>{dayFormat.format(new Date(lastVisit.nextVisitAt))}</p>
              </section>
            )}
          </article>
        )
      })}
    </>
  )
}
