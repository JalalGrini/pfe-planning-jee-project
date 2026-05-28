import { useState, useEffect } from 'react'
import { NavLink } from 'react-router-dom'
import { getProfesseurs, getIndisponibilites, addIndisponibilite, deleteIndisponibilite } from '../api/client'
import UiIcon from '../components/UiIcon'

function ProfIndispoPage() {
  const [professeurs, setProfesseurs] = useState([])
  const [selectedProfId, setSelectedProfId] = useState('')
  const [indisponibilites, setIndisponibilites] = useState([])
  
  const [date, setDate] = useState('')
  const [heureDebut, setHeureDebut] = useState('')
  const [heureFin, setHeureFin] = useState('')
  
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    fetchProfesseurs()
  }, [])

  useEffect(() => {
    if (selectedProfId) {
      fetchIndisponibilites(selectedProfId)
    } else {
      setIndisponibilites([])
    }
  }, [selectedProfId])

  const fetchProfesseurs = async () => {
    try {
      const data = await getProfesseurs()
      setProfesseurs(data)
    } catch (err) {
      console.error(err)
      setError("Erreur lors du chargement des professeurs")
    }
  }

  const fetchIndisponibilites = async (profId) => {
    try {
      const data = await getIndisponibilites(profId)
      setIndisponibilites(data)
    } catch (err) {
      console.error(err)
    }
  }

  const handleAdd = async (e) => {
    e.preventDefault()
    if (!selectedProfId || !date) return
    
    setLoading(true)
    setError('')
    try {
      const payload = {
        date,
        heureDebut: heureDebut || null,
        heureFin: heureFin || null
      }
      await addIndisponibilite(selectedProfId, payload)
      await fetchIndisponibilites(selectedProfId)
      setDate('')
      setHeureDebut('')
      setHeureFin('')
    } catch (err) {
      setError(err.message || "Erreur lors de l'ajout")
    } finally {
      setLoading(false)
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm("Voulez-vous supprimer cette indisponibilité ?")) return
    try {
      await deleteIndisponibilite(id)
      await fetchIndisponibilites(selectedProfId)
    } catch (err) {
      alert("Erreur lors de la suppression")
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Indisponibilités des Professeurs</h1>
        <p>Gérez les créneaux où les professeurs ne sont pas disponibles pour les soutenances.</p>
      </div>

      <section className="section">
        <div className="card card-static">
          <div className="form-grid">
            <label className="form-label" style={{ gridColumn: '1 / -1' }}>
              Sélectionner un professeur
              <select 
                value={selectedProfId} 
                onChange={(e) => setSelectedProfId(e.target.value)}
                style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: '1px solid #cbd5e1' }}
              >
                <option value="">-- Choisir un professeur --</option>
                {professeurs.map(prof => (
                  <option key={prof.id} value={prof.id}>
                    {prof.nom} {prof.prenom}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </div>
      </section>

      {selectedProfId && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: '20px' }}>
          <section className="section">
            <div className="section-header"><h2>Ajouter une indisponibilité</h2></div>
            <form className="card form-card card-static" onSubmit={handleAdd}>
              <div className="form-grid">
                <label className="form-label" style={{ gridColumn: '1 / -1' }}>
                  Date
                  <input type="date" value={date} onChange={(e) => setDate(e.target.value)} required />
                </label>
                <label className="form-label">
                  Heure de début
                  <input type="time" value={heureDebut} onChange={(e) => setHeureDebut(e.target.value)} />
                </label>
                <label className="form-label">
                  Heure de fin
                  <input type="time" value={heureFin} onChange={(e) => setHeureFin(e.target.value)} />
                </label>
              </div>
              <div className="form-actions" style={{ marginTop: '15px' }}>
                <button className="btn btn-primary" type="submit" disabled={loading || !date}>
                  {loading ? 'Ajout...' : 'Ajouter'}
                </button>
              </div>
              {error && <p className="inline-error" style={{ marginTop: '10px' }}>{error}</p>}
            </form>
          </section>

          <section className="section">
            <div className="section-header"><h2>Indisponibilités actuelles</h2></div>
            <div className="card table-card card-static">
              {indisponibilites.length === 0 ? (
                <div style={{ padding: '20px', textAlign: 'center', color: '#64748b' }}>
                  Aucune indisponibilité pour ce professeur.
                </div>
              ) : (
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Heure de début</th>
                        <th>Heure de fin</th>
                        <th>Action</th>
                      </tr>
                    </thead>
                    <tbody>
                      {indisponibilites.map((indispo) => (
                        <tr key={indispo.id}>
                          <td>{indispo.date}</td>
                          <td>{indispo.heureDebut || '-'}</td>
                          <td>{indispo.heureFin || '-'}</td>
                          <td>
                            <button 
                              onClick={() => handleDelete(indispo.id)}
                              style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer' }}
                              title="Supprimer"
                            >
                              <UiIcon name="delete" size={18} />
                              Supprimer
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
      )}
      
      <div className="form-actions" style={{ justifyContent: 'center', marginTop: '30px' }}>
        <NavLink className="btn btn-primary btn-lg" to="/binomes">
          Continuer vers les Binômes
        </NavLink>
      </div>
    </div>
  )
}

export default ProfIndispoPage
