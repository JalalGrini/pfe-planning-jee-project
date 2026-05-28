import { useEffect, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { downloadExport, listSoutenances, saveBlob } from '../api/client'

const planningOptions = [
  {
    type: 'excel',
    filename: 'planning_soutenances.xlsx',
    label: 'Export Excel',
    desc: 'Fichier xlsx avec le planning final',
    variant: 'btn-primary',
  },
  {
    type: 'pdf',
    filename: 'planning_soutenances.pdf',
    label: 'Export PDF',
    desc: 'Document PDF pret a imprimer',
    variant: 'btn-ghost',
  },
]

const affectationOptions = [
  {
    type: 'affectation-excel',
    filename: 'affectation_encadrants_etudiants.xlsx',
    label: 'Export Excel',
    desc: 'Fichier xlsx a telecharger',
    variant: 'btn-primary',
  },
  {
    type: 'affectation-pdf',
    filename: 'affectation_encadrants_etudiants.pdf',
    label: 'Export PDF',
    desc: 'Document PDF pret a imprimer',
    variant: 'btn-ghost',
  },
]

const downloadFile = async (type, filename, setError) => {
  try {
    const blob = await downloadExport(type)
    saveBlob(blob, filename)
    return true
  } catch (err) {
    setError(err.message || 'Erreur export')
    return false
  }
}

const ExportCards = ({ options, downloading, hasPlanning, onDownload }) => (
  <div className="guide-grid">
    {options.map((opt) => (
      <div key={opt.type} className="card feature-card" style={{ textAlign: 'center' }}>
        <h3>{opt.label}</h3>
        <p style={{ marginBottom: '16px' }}>{opt.desc}</p>
        <button
          className={`btn ${opt.variant}`}
          type="button"
          onClick={() => onDownload(opt.type, opt.filename)}
          disabled={!!downloading || hasPlanning !== true}
        >
          {downloading === opt.type ? 'Telechargement...' : `Telecharger ${opt.label}`}
        </button>
      </div>
    ))}
  </div>
)

function ExportsPage() {
  const [error, setError] = useState('')
  const [downloading, setDownloading] = useState('')
  const [hasPlanning, setHasPlanning] = useState(null)

  useEffect(() => {
    listSoutenances()
      .then((data) => {
        const planned = Array.isArray(data) ? data.some((item) => item.date) : false
        setHasPlanning(planned)
      })
      .catch(() => setHasPlanning(false))
  }, [])

  const handleDownload = async (type, filename) => {
    if (!hasPlanning) return
    setError('')
    setDownloading(type)
    await downloadFile(type, filename, setError)
    setDownloading('')
  }

  const handleBothPlanning = async () => {
    if (!hasPlanning) return
    setError('')
    setDownloading('planning-both')
    const okExcel = await downloadFile('excel', 'planning_soutenances.xlsx', setError)
    if (okExcel) {
      await downloadFile('pdf', 'planning_soutenances.pdf', setError)
    }
    setDownloading('')
  }

  const handleBothAffectation = async () => {
    if (!hasPlanning) return
    setError('')
    setDownloading('affectation-both')
    const okExcel = await downloadFile('affectation-excel', 'affectation_encadrants_etudiants.xlsx', setError)
    if (okExcel) {
      await downloadFile('affectation-pdf', 'affectation_encadrants_etudiants.pdf', setError)
    }
    setDownloading('')
  }

  if (hasPlanning === false) {
    return (
      <div className="page">
        <div className="empty-state">
          <h2 className="empty-state__title">Aucun planning genere</h2>
          <p className="empty-state__text">
            Vous devez generer le planning avant de telecharger les exports Excel ou PDF.
          </p>
          <NavLink className="btn btn-primary" to="/planning">
            Generer le planning
          </NavLink>
        </div>
      </div>
    )
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>Exports du planning</h1>
        <p>Telechargez le planning final en Excel ou PDF.</p>
      </div>

      <section className="section">
        <h3>Exports du planning</h3>
        <ExportCards
          options={planningOptions}
          downloading={downloading}
          hasPlanning={hasPlanning}
          onDownload={handleDownload}
        />
      </section>

      <section className="section" style={{ animationDelay: '0.1s' }}>
        <div className="card card-static" style={{ textAlign: 'center', padding: '28px' }}>
          <h3 style={{ marginBottom: '12px' }}>Telecharger les deux formats du planning</h3>
          <p className="muted" style={{ marginBottom: '18px' }}>
            Obtenez le planning Excel et PDF en un seul clic.
          </p>
          <button
            className="btn btn-outline"
            type="button"
            onClick={handleBothPlanning}
            disabled={!!downloading || hasPlanning !== true}
          >
            {downloading === 'planning-both' ? 'En cours...' : 'Planning: Excel + PDF'}
          </button>
        </div>
      </section>

      <section className="section" style={{ animationDelay: '0.2s' }}>
        <h3>Export d'affectation</h3>
        <ExportCards
          options={affectationOptions}
          downloading={downloading}
          hasPlanning={hasPlanning}
          onDownload={handleDownload}
        />
      </section>

      <section className="section" style={{ animationDelay: '0.3s' }}>
        <div className="card card-static" style={{ textAlign: 'center', padding: '28px' }}>
          <h3 style={{ marginBottom: '12px' }}>Telecharger les deux formats d'affectation</h3>
          <p className="muted" style={{ marginBottom: '18px' }}>
            Obtenez le fichier affectation en Excel et PDF en un seul clic.
          </p>
          <button
            className="btn btn-outline"
            type="button"
            onClick={handleBothAffectation}
            disabled={!!downloading || hasPlanning !== true}
          >
            {downloading === 'affectation-both' ? 'En cours...' : 'Affectation: Excel + PDF'}
          </button>
        </div>
      </section>

      {error && (
        <section className="section">
          <p className="inline-error">{error}</p>
        </section>
      )}

      <section className="section" style={{ animationDelay: '0.4s' }}>
        <div className="card card-static">
          <p className="muted">
            Les exports sont generes uniquement a partir des soutenances planifiees.
          </p>
        </div>
      </section>
    </div>
  )
}

export default ExportsPage
