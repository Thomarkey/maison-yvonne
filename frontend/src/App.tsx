import { Home } from './Home'
import { AppShell } from './app/AppShell'

/** Same bundle: the public website on /, the pilot app on /app. */
export function App() {
  return window.location.pathname.startsWith('/app') ? <AppShell /> : <Home />
}
