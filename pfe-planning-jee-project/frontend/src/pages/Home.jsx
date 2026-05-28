import { NavLink } from 'react-router-dom'
import UiIcon from '../components/UiIcon'

const workflows = [
  { title: 'Importer les donnees', text: 'Chargez vos fichiers Excel : etudiants, professeurs et salles disponibles.' },
  { title: 'Valider les donnees', text: 'Verifiez la coherence et les statistiques des donnees importees.' },
  { title: 'Generer le planning', text: 'Lancez la planification automatique avec les contraintes.' },
  { title: 'Exporter les resultats', text: 'Telechargez le planning Excel/PDF et les proces-verbaux.' },
]

const features = [
  { icon: 'upload', color: 'green', title: 'Import multi-fichiers', text: 'Import separe des etudiants, professeurs et salles.' },
  { icon: 'planning', color: 'blue', title: 'Planification intelligente', text: 'Attribution automatique des jurys, salles et creneaux.' },
  { icon: 'dashboard', color: 'warm', title: 'Dashboard', text: 'Statistiques claires pour suivre la planification.' },
]

function Home() {
  return (
    <div className="page">
      <section className="hero-section">
        <div className="hero-content">
          <div className="pill">Session 2025-2026</div>
          <h1>Planifiez les soutenances PFE</h1>
          <p className="hero-text">
            Une plateforme academique complete pour organiser, planifier et gerer les soutenances de Projet de Fin d&apos;Etudes.
          </p>
          <div className="hero-actions">
            <NavLink className="btn btn-primary btn-lg" to="/import">
              Commencer
            </NavLink>
          </div>
        </div>
      </section>

      <section className="section" style={{ animationDelay: '0.2s' }}>
        <div className="section-header">
          <div>
            <h2>Comment ca marche ?</h2>
          </div>
        </div>
        <div className="workflow-grid">
          {workflows.map((item, i) => (
            <div key={item.title} className="card workflow-step">
              <div className="step-number">{i + 1}</div>
              <h3>{item.title}</h3>
              <p>{item.text}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="section" style={{ animationDelay: '0.4s' }}>
        <div className="section-header">
          <div>
            <h2>Fonctionnalites de la plateforme</h2>
          </div>
        </div>
        <div className="features-grid">
          {features.map((item) => (
            <div key={item.title} className="card feature-card">
              <div className={`feature-icon feature-icon--${item.color}`}>
                <UiIcon name={item.icon} />
              </div>
              <h3>{item.title}</h3>
              <p>{item.text}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="section" style={{ animationDelay: '0.6s' }}>
        <div className="about-section">
          <h2>A propos de la plateforme</h2>
          <p>
            PFE Planning est une application web academique developpee dans le cadre du Projet de Fin d&apos;Etudes a l&apos;ENSAH - Universite Abdelmalek Essaadi.
            Elle automatise et optimise la planification des soutenances avec une repartition equitable des jurys et une gestion efficace des salles.
          </p>
        </div>
      </section>
    </div>
  )
}

export default Home
