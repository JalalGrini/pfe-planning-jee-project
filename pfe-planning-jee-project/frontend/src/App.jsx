import { NavLink, Route, Routes } from 'react-router-dom'
import './App.css'
import Home from './pages/Home'
import ImportPage from './pages/ImportPage'
import ValidationPage from './pages/ValidationPage'
import PlanningPage from './pages/PlanningPage'
import RecherchePage from './pages/RecherchePage'
import ExportsPage from './pages/ExportsPage'
import ProfIndispoPage from './pages/ProfIndispoPage'
import BinomePage from './pages/BinomePage'

const navLinks = [
  { to: '/', label: 'Accueil', end: true },
  { to: '/import', label: 'Import Excel' },
  { to: '/indisponibilites', label: 'Indisponibilités' },
  { to: '/binomes', label: 'Binômes' },
  { to: '/planning', label: 'Planning' },
  { to: '/recherche', label: 'Recherche' },
  { to: '/exports', label: 'Exports' },
]

const assetUrl = (path) => `${import.meta.env.BASE_URL}${path}`
const ensahUrl = 'https://ensah.ma/'
const uaeUrl = 'https://www.uae.ac.ma/'

function App() {
  return (
    <div className="app">
      <header className="top-bar">
        <div className="container top-bar__content">
          <div className="brand">
            <a
              href={ensahUrl}
              className="brand-logo-link"
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Open ENSAH website"
            >
              <img src={assetUrl('ensah.png')} alt="ENSAH" className="brand-logo" />
            </a>
            <div className="brand-text">
              <span className="brand-kicker">PFE Planning</span>
              <span className="brand-title">Gestion des soutenances</span>
            </div>
          </div>

          <nav className="nav">
            {navLinks.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                end={item.end}
                className={({ isActive }) =>
                  isActive ? 'nav-link active' : 'nav-link'
                }
              >
                {item.label}
              </NavLink>
            ))}
          </nav>

          <div className="brand brand--right">
            <a
              href={uaeUrl}
              className="brand-logo-link"
              target="_blank"
              rel="noopener noreferrer"
              aria-label="Open UAE website"
            >
              <img src={assetUrl('uae.png')} alt="UAE" className="brand-logo" />
            </a>
          </div>
        </div>
      </header>

      <main className="main">
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/import" element={<ImportPage />} />
          <Route path="/validation" element={<ValidationPage />} />
          <Route path="/indisponibilites" element={<ProfIndispoPage />} />
          <Route path="/binomes" element={<BinomePage />} />
          <Route path="/planning" element={<PlanningPage />} />
          <Route path="/recherche" element={<RecherchePage />} />
          <Route path="/exports" element={<ExportsPage />} />
          <Route path="*" element={<Home />} />
        </Routes>
      </main>

      <footer className="footer">
        <div className="container footer__content">
          <div>
            <p className="footer-title">Plateforme PFE Planning</p>
            <p className="footer-text">
              ENSAH - Universite Abdelmalek Essaadi
            </p>
          </div>
          <div className="footer-meta">
            <span>Taha Lamhandi</span>
            <span>Ayoub Nassih</span>
            <span>Saad Abs</span>
            <span>Jalal Grini</span>
          </div>
        </div>
      </footer>
    </div>
  )
}

export default App
