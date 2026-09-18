import { useEffect, useState } from 'react'

export function App() {
  const [status, setStatus] = useState('…')

  useEffect(() => {
    fetch('/api/health')
      .then((r) => r.json())
      .then((b) => setStatus(b.status))
      .catch(() => setStatus('unreachable'))
  }, [])

  return (
    <main>
      <h1>Maison Yvonne</h1>
      <p>backend: {status}</p>
    </main>
  )
}
