import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { importComplet } from '../api/client'
import UiIcon from '../components/UiIcon'

function ImportPage() {
  const navigate = useNavigate()
  const [file, setFile] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleFileChange = (selectedFile) => {
    setFile(selectedFile)
    setError('')
  }

  const handleDrop = (e) => {
    e.preventDefault()
    const droppedFile = e.dataTransfer.files[0]
    if (droppedFile) handleFileChange(droppedFile)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!file) {
      setError('Veuillez selectionner un fichier Excel avant de valider.')
      return
    }
    setLoading(true)
    setError('')
    try {
      const result = await importComplet(file)
      navigate('/validation', {
        state: {
          importResult: result,
          files: {
            fichierUnique: file.name,
          },
        },
      })
    } catch (err) {
      setError(err.message || "Erreur pendant l'import. Verifiez votre fichier Excel.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Import des donnees</h1>
        <p>Chargez votre fichier Excel contenant toutes les feuilles (Etudiants, Professeurs, Salles) pour demarrer la planification des soutenances.</p>
      </div>

      <form className="section" onSubmit={handleSubmit}>
        <div className="section-header">
          <div>
            <h2>Fichier requis</h2>
          </div>
        </div>

        <div className="features-grid">
          <div
            className={`card upload-zone ${file ? 'has-file' : ''}`}
            onDragOver={(e) => e.preventDefault()}
            onDrop={handleDrop}
            onClick={() => document.getElementById('file-upload').click()}
            style={{ gridColumn: '1 / -1', maxWidth: '600px', margin: '0 auto' }}
          >
            <div className="upload-icon"><UiIcon name="document" size={40} /></div>
            <div className="upload-label">Fichier Excel Global</div>
            <div className="upload-hint">Feuilles requises: Etudiants, Professeurs, Salles (.xlsx, .xls)</div>

            {file && (
              <div className="upload-file-name" style={{ marginTop: '15px', fontWeight: 'bold' }}>
                Fichier selectionne : {file.name}
              </div>
            )}

            <input
              id="file-upload"
              type="file"
              accept=".xlsx,.xls"
              style={{ display: 'none' }}
              onChange={(e) => handleFileChange(e.target.files[0] || null)}
            />
          </div>
        </div>

        {error && <p className="inline-error" style={{ textAlign: 'center', marginTop: '15px' }}>{error}</p>}

        <div className="form-actions" style={{ justifyContent: 'center' }}>
          <button className="btn btn-primary btn-lg" type="submit" disabled={loading || !file}>
            {loading ? 'Validation en cours...' : 'Valider les donnees'}
          </button>
        </div>
      </form>
    </div>
  )
}

export default ImportPage
