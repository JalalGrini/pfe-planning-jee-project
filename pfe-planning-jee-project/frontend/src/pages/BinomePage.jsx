import { useState, useEffect } from 'react'
import { getBinomes, getEtudiantsDisponibles, creerBinomeManuel } from '../api/client'
import UiIcon from '../components/UiIcon'

function BinomePage() {
  const [binomes, setBinomes] = useState([])
  const [etudiants, setEtudiants] = useState([])
  
  const [showForm, setShowForm] = useState(false)
  const [etudiant1, setEtudiant1] = useState('')
  const [etudiant2, setEtudiant2] = useState('')
  const [sujet, setSujet] = useState('')

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [actionMessage, setActionMessage] = useState('')

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    setLoading(true)
    setError('')
    try {
      const [binomesData, etudiantsData] = await Promise.all([
        getBinomes(),
        getEtudiantsDisponibles()
      ])
      setBinomes(binomesData || [])
      setEtudiants(etudiantsData || [])
    } catch (err) {
      console.error(err)
      setError("Erreur lors du chargement des données")
    } finally {
      setLoading(false)
    }
  }

  const handleCreateBinome = async (e) => {
    e.preventDefault()
    if (!etudiant1 || !etudiant2 || !sujet) {
      setError('Veuillez sélectionner 2 étudiants et saisir un sujet.')
      return
    }
    if (etudiant1 === etudiant2) {
      setError('Vous devez sélectionner 2 étudiants différents.')
      return
    }

    setLoading(true)
    setError('')
    try {
      const payload = {
        etudiantIds: [parseInt(etudiant1), parseInt(etudiant2)],
        sujet
      }
      await creerBinomeManuel(payload)
      setActionMessage('Binôme créé avec succès !')
      setTimeout(() => setActionMessage(''), 3000)
      
      // Reset form
      setEtudiant1('')
      setEtudiant2('')
      setSujet('')
      setShowForm(false)
      
      // Refresh data
      await fetchData()
    } catch (err) {
      setError(err.message || 'Erreur lors de la création du binôme')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Gestion des Binômes</h1>
        <p>Créez manuellement les binômes d'étudiants pour les soutenances de PFE.</p>
      </div>

      {actionMessage && (
        <div style={{ padding: '10px', backgroundColor: '#dcfce7', color: '#166534', borderRadius: '4px', marginBottom: '15px' }}>
          {actionMessage}
        </div>
      )}
      
      <section className="section" style={{ marginBottom: '20px' }}>
        {!showForm ? (
          <button className="btn btn-primary" onClick={() => setShowForm(true)}>
            <UiIcon name="add" size={16} /> Créer un binôme
          </button>
        ) : (
          <form className="card form-card card-static" onSubmit={handleCreateBinome}>
            <div className="section-header"><h2>Nouveau Binôme</h2></div>
            <div className="form-grid">
              <label className="form-label" style={{ gridColumn: '1 / -1' }}>
                Étudiant 1
                <select 
                  value={etudiant1} 
                  onChange={(e) => setEtudiant1(e.target.value)}
                  style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: '1px solid #cbd5e1' }}
                  required
                >
                  <option value="">-- Choisir un étudiant --</option>
                  {etudiants.map(etu => (
                    <option key={etu.id} value={etu.id} disabled={etu.id.toString() === etudiant2}>
                      {etu.nom} {etu.prenom} ({etu.cne})
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-label" style={{ gridColumn: '1 / -1' }}>
                Étudiant 2
                <select 
                  value={etudiant2} 
                  onChange={(e) => setEtudiant2(e.target.value)}
                  style={{ width: '100%', padding: '8px', marginTop: '5px', borderRadius: '4px', border: '1px solid #cbd5e1' }}
                  required
                >
                  <option value="">-- Choisir un étudiant --</option>
                  {etudiants.map(etu => (
                    <option key={etu.id} value={etu.id} disabled={etu.id.toString() === etudiant1}>
                      {etu.nom} {etu.prenom} ({etu.cne})
                    </option>
                  ))}
                </select>
              </label>

              <label className="form-label" style={{ gridColumn: '1 / -1' }}>
                Sujet PFE
                <input 
                  type="text" 
                  value={sujet} 
                  onChange={(e) => setSujet(e.target.value)} 
                  placeholder="Ex: Application de gestion de PFE"
                  required 
                />
              </label>
            </div>
            
            {error && <p className="inline-error" style={{ marginTop: '10px' }}>{error}</p>}
            
            <div className="form-actions" style={{ marginTop: '15px', display: 'flex', gap: '10px' }}>
              <button className="btn btn-primary" type="submit" disabled={loading}>
                {loading ? 'Création...' : 'Confirmer et Créer'}
              </button>
              <button className="btn btn-secondary" type="button" onClick={() => setShowForm(false)} disabled={loading}>
                Annuler
              </button>
            </div>
          </form>
        )}
      </section>

      <section className="section">
        <div className="card table-card card-static">
          <div className="section-header" style={{ padding: '20px 20px 0' }}>
            <h2>Liste des binômes créés</h2>
            <button className="btn btn-secondary btn-sm" onClick={fetchData} disabled={loading}>
              Actualiser
            </button>
          </div>

          {loading && !showForm ? (
            <div style={{ padding: '20px', textAlign: 'center' }}>Chargement...</div>
          ) : binomes.length === 0 ? (
            <div style={{ padding: '20px', textAlign: 'center', color: '#64748b' }}>
              Aucun binôme n'a été créé pour le moment.
            </div>
          ) : (
            <div className="table-wrapper" style={{ marginTop: '10px' }}>
              <table>
                <thead>
                  <tr>
                    <th>Sujet PFE</th>
                    <th>Étudiants</th>
                    <th>Statut</th>
                  </tr>
                </thead>
                <tbody>
                  {binomes.map((binome, index) => (
                    <tr key={index}>
                      <td style={{ maxWidth: '300px', whiteSpace: 'normal' }}>{binome.sujet}</td>
                      <td>
                        <ul style={{ margin: 0, paddingLeft: '20px' }}>
                          {binome.etudiants.map(etu => (
                            <li key={etu.id}>{etu.nom} {etu.prenom} ({etu.cne})</li>
                          ))}
                        </ul>
                      </td>
                      <td>
                        <span style={{ 
                          padding: '4px 8px', 
                          borderRadius: '12px', 
                          fontSize: '12px', 
                          fontWeight: 'bold',
                          backgroundColor: '#dcfce7',
                          color: '#166534'
                        }}>
                          Créé
                        </span>
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

export default BinomePage
