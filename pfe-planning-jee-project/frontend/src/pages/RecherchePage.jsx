import { useEffect, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { downloadEvaluation, listSoutenances, saveBlob, searchByEncadrant } from '../api/client'

const formatProf = (prof) => {
  if (!prof) return '-'
  return `${prof.nom || ''} ${prof.prenom || ''}`.trim()
}

const formatStudents = (etudiants) => {
  if (!etudiants || etudiants.length === 0) return '-'
  return etudiants.map((e) => `${e.nom || ''} ${e.prenom || ''}`.trim()).join(' & ')
}

const formatHour = (v) => {
  if (!v && v !== 0) return '-'
  const s = String(v).trim()
  if (/^\d{1,2}:\d{2}(:\d{2})?$/.test(s)) return `${s.slice(0, 5)}H`
  const comma = s.match(/^(\d{1,2}),0+$/)
  if (comma) return `${comma[1]}:00H`
  const dot = s.match(/^(\d{1,2})\.0+$/)
  if (dot) return `${dot[1]}:00H`
  if (/^\d{1,2}$/.test(s)) return `${s}:00H`
  return s.replace(',', ':') + 'H'
}

const buildFileName = (item) => {
  if (!item || !item.etudiants || item.etudiants.length === 0) {
    return `Fiche_Evaluation_PFE_Soutenance_${item.id}.docx`
  }
  const [first] = item.etudiants
  const nom = (first.nom || 'Etudiant').trim()
  const prenom = (first.prenom || '').trim()
  return `Fiche_Evaluation_PFE_${nom}_${prenom}.docx`.replace(/\s+/g, '_')
}

function RecherchePage() {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [hasPlanning, setHasPlanning] = useState(null)

  useEffect(() => {
    listSoutenances()
      .then((data) => {
        const planned = Array.isArray(data) ? data.some((s) => s.date) : false
        setHasPlanning(planned)
      })
      .catch(() => setHasPlanning(false))
  }, [])

  const handleSearch = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const data = await searchByEncadrant(query)
      setResults(data)
    } catch (err) {
      setError(err.message || 'Erreur lors de la recherche')
    } finally {
      setLoading(false)
    }
  }

  const handleDownload = async (item) => {
    try {
      const blob = await downloadEvaluation(item.id)
      saveBlob(blob, buildFileName(item))
    } catch (err) {
      setError(err.message || 'Erreur de telechargement')
    }
  }

  if (hasPlanning === false) {
    return (
      <div className="page">
        <div className="empty-state">
          <h2 className="empty-state__title">Aucun planning genere</h2>
          <p className="empty-state__text">
            Vous devez d'abord generer un planning avant de pouvoir rechercher les soutenances.
          </p>
          <NavLink className="btn btn-primary" to="/planning">
            Generer le planning
          </NavLink>
        </div>
      </div>
    )
  }

  if (hasPlanning === null) {
    return (
      <div className="page">
        <div className="empty-state">
          <h2 className="empty-state__title">Chargement...</h2>
        </div>
      </div>
    )
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Recherche des soutenances</h1>
        <p>Recherchez par nom/prenom d'etudiant ou d'encadrant, dans les deux ordres.</p>
      </div>

      <section className="section">
        <form className="card form-card card-static" onSubmit={handleSearch}>
          <div className="form-grid form-grid--wide">
            <label className="form-label">
              Nom / Prenom (etudiant ou encadrant)
              <input
                type="text"
                placeholder="Entrer nom ou prenom ou les deux"
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </label>
            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Recherche...' : 'Lancer la recherche'}
            </button>
          </div>
          {error && <p className="inline-error">{error}</p>}
        </form>
      </section>

      <section className="section" style={{ animationDelay: '0.15s' }}>
        <div className="card table-card card-static">
          {results.length === 0 ? (
            <p className="muted" style={{ padding: '24px' }}>Aucun resultat pour le moment.</p>
          ) : (
            <div className="table-wrapper">
              <table>
                <thead>
                  <tr>
                    <th>Etudiant</th>
                    <th>Encadrant</th>
                    <th>Jury 1</th>
                    <th>Jury 2</th>
                    <th>Salle</th>
                    <th>Heure</th>
                    <th>PV</th>
                  </tr>
                </thead>
                <tbody>
                  {results.map((item) => (
                    <tr key={item.id}>
                      <td>{formatStudents(item.etudiants)}</td>
                      <td>{formatProf(item.president)}</td>
                      <td>{formatProf(item.jurys?.[0])}</td>
                      <td>{formatProf(item.jurys?.[1])}</td>
                      <td>{item.salle ? item.salle.nom : '-'}</td>
                      <td>{formatHour(item.heureDebut)}</td>
                      <td>
                        <button className="btn btn-mini" type="button" onClick={() => handleDownload(item)}>
                          Telecharger
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </section>
    </div>
  )
}

export default RecherchePage
