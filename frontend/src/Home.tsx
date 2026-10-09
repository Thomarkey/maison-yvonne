const NAV = [
  'Home',
  'Hoe het werkt',
  'Maaltijden',
  'Voor de familie',
  'Ons verhaal',
  'Contact',
]

const PILLARS = [
  { icon: 'bowl', title: 'Voeden', text: 'Verse, evenwichtige maaltijden met zorg bereid.' },
  { icon: 'house', title: 'Bezoeken', text: 'Een vertrouwd gezicht aan huis.' },
  { icon: 'heart', title: 'Opmerken', text: 'Met aandacht voor wat telt.' },
  { icon: 'people', title: 'Verbinden', text: 'Geruststelling, ook vanop afstand.' },
] as const

const PATHS: Record<(typeof PILLARS)[number]['icon'], string> = {
  bowl: 'M4 14a8 8 0 0 0 16 0H4Zm8-4c2-2-2-3 0-6',
  house: 'M3 11 12 4l9 7v9H3v-9Zm6 9v-6h6v6',
  heart: 'M12 20S3 14.5 3 9a4.5 4.5 0 0 1 9-1.8A4.5 4.5 0 0 1 21 9c0 5.5-9 11-9 11Z',
  people: 'M2 20a5 5 0 0 1 10 0M7 11a3 3 0 1 0 0-6 3 3 0 0 0 0 6Zm8 9a5 5 0 0 1 7-4.6M17 11a3 3 0 1 0 0-6',
}

function Icon({ name }: { name: keyof typeof PATHS }) {
  return (
    <svg width="34" height="34" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.4" aria-hidden>
      <path d={PATHS[name]} />
    </svg>
  )
}

function Photo({ label, tone = '', className = '' }: { label: string; tone?: string; className?: string }) {
  return <div className={`photo ${tone} ${className}`}>{label}</div>
}

export function Home() {
  return (
    <>
      <nav className="nav">
        <a className="logo" href="/">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="var(--terracotta)" strokeWidth="1.4" aria-hidden>
            <path d={PATHS.bowl} />
          </svg>
          <span>
            MAISON
            <br />
            YVONNE
          </span>
        </a>
        <ul className="nav-links">
          {NAV.map((item) => (
            <li key={item}>
              <a href="#" aria-current={item === 'Home' ? 'page' : undefined}>
                {item}
              </a>
            </li>
          ))}
        </ul>
        <a className="btn" href="#interesse">
          Interesse?
        </a>
        <a className="btn" href="/app">
          Aanmelden
        </a>
        <div className="lang">
          <a href="#">NL</a>
          <span aria-hidden>|</span>
          <a href="#">FR</a>
        </div>
      </nav>

      <header className="hero">
        <div>
          <p className="eyebrow">Voor wie een leven lang heeft gezorgd.</p>
          <h1>
            We brengen
            <br />
            meer dan eten.
          </h1>
          <p>
            Drie verse maaltijden, een warm bezoek en een oprechte update voor de mensen die je
            dierbaar zijn.
          </p>
          <a className="btn" href="#hoe-het-werkt">
            Ontdek hoe het werkt <span aria-hidden>→</span>
          </a>
          <p className="claims">
            <span>Goed eten</span>
            <span>Echte aandacht</span>
            <span>Gewoon thuis</span>
          </p>
        </div>
        <Photo className="hero-photo" label="foto: maaltijddoos op gedekte tafel" />
      </header>

      <section className="pillars">
        <ul>
          {PILLARS.map(({ icon, title, text }) => (
            <li key={title}>
              <Icon name={icon} />
              <h3>{title}</h3>
              <p>{text}</p>
            </li>
          ))}
        </ul>
      </section>

      <section className="band">
        <div className="band-text">
          <p className="eyebrow">Onze maaltijden</p>
          <h2>
            Echt eten.
            <br />
            Echt lekker.
            <br />
            Gewoon thuis.
          </h2>
          <p>
            Drie verse maaltijden per week, met seizoensgebonden ingrediënten en de smaak van een
            goede keuken.
          </p>
          <a className="textlink" href="#maaltijden">
            Ontdek onze maaltijden <span aria-hidden>→</span>
          </a>
        </div>
        <Photo tone="photo--sage" label="foto: drie genummerde maaltijddozen" />
        <Photo tone="photo--cream" label="foto: Yvonne met een kop koffie" />
        <div className="band-text">
          <p className="eyebrow">Voor de familie</p>
          <h2>
            Dichtbij,
            <br />
            ook vanop afstand.
          </h2>
          <p>Na elk bezoek ontvangt de familie een korte, persoonlijke update via onze app.</p>
          <a className="textlink" href="#app">
            Meer over de app <span aria-hidden>→</span>
          </a>
        </div>
      </section>

      <section className="band band--story">
        <div className="band-text">
          <p className="eyebrow">Ons verhaal</p>
          <h2>
            Yasmina, Louis
            <br />
            en Yvonne.
          </h2>
          <p>
            Gedreven door persoonlijke ervaring en een grote maatschappelijke nood, bouwen we aan een
            warmer morgen.
          </p>
          <a className="textlink" href="#verhaal">
            Lees ons verhaal <span aria-hidden>→</span>
          </a>
        </div>
        <Photo tone="photo--cream" label="foto: verse soep in een schaal" />
        <blockquote className="quote">“Goed eten brengt mensen samen.”</blockquote>
        <Photo tone="photo--sage" label="foto: linnen draagzak met logo" />
      </section>

      <footer>Maison Yvonne — Goed eten, brighter days.</footer>
    </>
  )
}
