import { useLocation, useNavigate, NavLink } from 'react-router-dom'
import UiIcon from '../components/UiIcon'

function ValidationPage() {
  const location = useLocation()
  const navigate = useNavigate()
  const { importResult, files } = location.state || {}

  if (!importResult) {
    return (
      <div className="page">
        <div className="empty-state">
          <h2 className="empty-state__title">Aucune donnee importee</h2>
          <p className="empty-state__text">
            Vous devez d&apos;abord importer vos fichiers Excel avant d&apos;acceder a la validation.
          </p>
          <NavLink className="btn btn-primary" to="/import">
            Retour a l&apos;importation
          </NavLink>
        </div>
      </div>
    )
  }

  const hasErrors = importResult.errors && importResult.errors.length > 0
  const stats = importResult.stats || {}

  const dashboardItems = [
    { icon: 'students', label: 'Etudiants', value: stats.nbEtudiants || 0, color: 'green' },
    { icon: 'professor', label: 'Professeurs', value: stats.nbProfesseurs || 0, color: 'blue' },
    { icon: 'room', label: 'Salles', value: stats.nbSalles || 0, color: 'purple' },
    { icon: 'filiere', label: 'Filieres', value: stats.nbFilieres || 0, color: 'warm' },
  ]

  return (
    <div className="page">
      <div className="page-header">
        <h1>Validation des donnees</h1>
        <p>Verifiez les statistiques de vos donnees importees avant de continuer.</p>
      </div>

      {hasErrors ? (
        <div className="inline-error" style={{ fontSize: '15px' }}>
          Des erreurs ont ete detectees dans vos fichiers. Veuillez corriger et reimporter.
        </div>
      ) : (
        <div className="inline-success" style={{ fontSize: '15px' }}>
          Toutes les donnees ont ete importees avec succes. Vous pouvez continuer.
        </div>
      )}

      <section className="section">
        <div className="section-header">
          <div>
            <h2>Resume des donnees</h2>
            <p className="section-subtitle">
              Vue d&apos;ensemble des donnees chargees depuis vos fichiers Excel.
            </p>
          </div>
        </div>
        <div className="dashboard-grid">
          {dashboardItems.map((item) => (
            <div key={item.label} className={`card dashboard-card dashboard-card--${item.color}`}>
              <div className="dashboard-icon"><UiIcon name={item.icon} size={28} /></div>
              <div className="dashboard-value">{item.value}</div>
              <div className="dashboard-label">{item.label}</div>
            </div>
          ))}
        </div>
      </section>

      {files && (
        <section className="section">
          <div className="section-header">
            <div>
              <h2>Fichiers importes</h2>
              <p className="section-subtitle">Les fichiers Excel qui ont ete traites.</p>
            </div>
          </div>
          <div className="features-grid">
            <div className="card feature-card">
              <div className="feature-icon feature-icon--green"><UiIcon name="students" /></div>
              <h3>Etudiants</h3>
              <p>{files.etudiants}</p>
            </div>
            <div className="card feature-card">
              <div className="feature-icon feature-icon--blue"><UiIcon name="professor" /></div>
              <h3>Professeurs</h3>
              <p>{files.professeurs}</p>
            </div>
            <div className="card feature-card">
              <div className="feature-icon feature-icon--purple"><UiIcon name="room" /></div>
              <h3>Salles</h3>
              <p>{files.salles}</p>
            </div>
          </div>
        </section>
      )}

      {hasErrors && (
        <section className="section">
          <div className="section-header">
            <div>
              <h2>Erreurs detectees</h2>
              <p className="section-subtitle">Corrigez ces problemes et relancez l&apos;importation.</p>
            </div>
          </div>
          <div className="card">
            <ul className="anomalies-list">
              {importResult.errors.map((err, i) => (
                <li key={i} style={{ color: 'var(--accent-red)', marginBottom: '6px' }}>{err}</li>
              ))}
            </ul>
          </div>
        </section>
      )}

      {stats.details && (
        <section className="section">
          <div className="section-header">
            <div>
              <h2>Details supplementaires</h2>
              <p className="section-subtitle">Informations detaillees sur les donnees.</p>
            </div>
          </div>
          <div className="guide-grid">
            {stats.details.filieres && (
              <div className="card guide-card">
                <h3>Filieres detectees</h3>
                <div className="tag-grid" style={{ marginTop: '12px' }}>
                  {stats.details.filieres.map((f) => (
                    <span className="tag" key={f}>{f}</span>
                  ))}
                </div>
              </div>
            )}
            {stats.details.specialites && (
              <div className="card guide-card">
                <h3>Specialites des professeurs</h3>
                <div className="tag-grid" style={{ marginTop: '12px' }}>
                  {stats.details.specialites.map((s) => (
                    <span className="tag" key={s}>{s}</span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </section>
      )}

      <div className="form-actions" style={{ justifyContent: 'center', paddingTop: '12px' }}>
        {hasErrors ? (
          <button className="btn btn-primary btn-lg" onClick={() => navigate('/import')}>
            Retour a l&apos;importation
          </button>
        ) : (
          <>
            <NavLink className="btn btn-primary btn-lg" to="/indisponibilites">
              Continuer vers les Indisponibilites
            </NavLink>
            <button className="btn btn-ghost" onClick={() => navigate('/import')}>
              Reimporter
            </button>
          </>
        )}
      </div>
    </div>
  )
}

export default ValidationPage
