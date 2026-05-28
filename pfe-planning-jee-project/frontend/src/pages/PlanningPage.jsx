import { useEffect, useMemo, useState } from 'react'
import { NavLink } from 'react-router-dom'
import { generatePlanning, getStats, listSoutenances } from '../api/client'
import UiIcon from '../components/UiIcon'

const formatDateInput = (date) => {
  const pad = (v) => String(v).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`
}

const formatProf = (prof) => (prof ? `${prof.nom || ''} ${prof.prenom || ''}`.trim() : '-')
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

const parseHourNumber = (value) => {
  if (value === null || value === undefined) return null
  const s = String(value).trim()
  const hhmm = s.match(/^(\d{1,2}):(\d{2})/)
  if (hhmm) return Number(hhmm[1])
  const comma = s.match(/^(\d{1,2}),0+$/)
  if (comma) return Number(comma[1])
  const dot = s.match(/^(\d{1,2})\.0+$/)
  if (dot) return Number(dot[1])
  if (/^\d{1,2}$/.test(s)) return Number(s)
  return null
}

const normalizeRoom = (value) => {
  if (!value) return '-'
  const t = String(value).toUpperCase()
  const m1 = t.match(/\b([AB])\s*0*(\d{1,2})\b/)
  if (m1) return `${m1[1]}${String(m1[2]).padStart(2, '0')}`
  const m2 = t.match(/\b(\d{3})\b/)
  if (m2) {
    const n = Number(m2[1])
    const bloc = n >= 200 ? 'B' : 'A'
    return `${bloc}${String(n % 100).padStart(2, '0')}`
  }
  return t.replace('SALLE', '').trim()
}

function PlanningPage() {
  const tomorrow = new Date()
  tomorrow.setDate(tomorrow.getDate() + 1)
  
  const [startDate, setStartDate] = useState(formatDateInput(tomorrow))
  const [endDate, setEndDate] = useState(formatDateInput(tomorrow))
  const [startHour, setStartHour] = useState(9)
  const [endHour, setEndHour] = useState(18)
  const [maxSoutenancesParCreneau, setMaxSoutenancesParCreneau] = useState(3)
  const [daysOfWeek, setDaysOfWeek] = useState(['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'])
  const [specificDatesStr, setSpecificDatesStr] = useState('')
  const [allowedHoursStr, setAllowedHoursStr] = useState('')

  const [result, setResult] = useState(null)
  const [planned, setPlanned] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [hasData, setHasData] = useState(null)

  useEffect(() => {
    getStats()
      .then((d) => setHasData(((d.nbEtudiants || 0) + (d.nbProfesseurs || 0)) > 0))
      .catch(() => setHasData(false))
    listSoutenances()
      .then((all) => setPlanned((all || []).filter((x) => x.date)))
      .catch(() => {})
  }, [])

  const handleDayToggle = (day) => {
    setDaysOfWeek((prev) => 
      prev.includes(day) ? prev.filter((d) => d !== day) : [...prev, day]
    )
  }

  const handleGenerate = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    setResult(null)
    
    const specificDates = specificDatesStr ? specificDatesStr.split(',').map(s => s.trim()).filter(s => s) : []
    const allowedHours = allowedHoursStr ? allowedHoursStr.split(',').map(s => parseInt(s.trim())).filter(n => !isNaN(n)) : []

    const payload = {
      startDate,
      endDate,
      excludeWeekends: false, // We control weekends via daysOfWeek selection now
      startHour,
      endHour,
      maxSoutenancesParCreneau,
      daysOfWeek,
      specificDates: specificDates.length > 0 ? specificDates : null,
      allowedHours: allowedHours.length > 0 ? allowedHours : null
    }

    try {
      const data = await generatePlanning(payload)
      setResult(data)
      const all = await listSoutenances()
      setPlanned((all || []).filter((x) => x.date))
    } catch (err) {
      setError(err.message || 'Erreur lors de la génération du planning')
    } finally {
      setLoading(false)
    }
  }

  const byDay = useMemo(() => planned.reduce((a, s) => { a[s.date] = (a[s.date] || 0) + 1; return a }, {}), [planned])
  const maxDay = Math.max(1, ...Object.values(byDay))

  const byRoom = useMemo(() => planned.reduce((a, s) => {
    const r = normalizeRoom(s?.salle?.nom)
    a[r] = (a[r] || 0) + 1
    return a
  }, {}), [planned])
  const totalRoom = Math.max(1, Object.values(byRoom).reduce((x, y) => x + y, 0))
  const roomEntries = Object.entries(byRoom)
  const pieStops = roomEntries.reduce((acc, [, count], i) => {
    const prev = acc.length ? acc[acc.length - 1].end : 0
    const end = prev + (count / totalRoom) * 360
    acc.push({ color: ['#8ecae6', '#90be6d', '#f9c74f', '#f9844a', '#577590'][i % 5], start: prev, end })
    return acc
  }, [])
  const pieGradient = `conic-gradient(${pieStops.map((s) => `${s.color} ${s.start}deg ${s.end}deg`).join(',') || '#dbeafe 0deg 360deg'})`

  const byFiliere = useMemo(() => planned.reduce((a, s) => {
    const name = s?.filiereNom || s?.etudiants?.[0]?.filiereNom || 'Inconnue'
    a[name] = (a[name] || 0) + 1
    return a
  }, {}), [planned])
  const totalFiliere = Math.max(1, Object.values(byFiliere).reduce((x, y) => x + y, 0))
  const filiereEntries = Object.entries(byFiliere)
  const filiereStops = filiereEntries.reduce((acc, [, count], i) => {
    const prev = acc.length ? acc[acc.length - 1].end : 0
    const end = prev + (count / totalFiliere) * 360
    acc.push({ color: ['#c4b5fd', '#86efac', '#93c5fd', '#fcd34d', '#fca5a5', '#67e8f9'][i % 6], start: prev, end })
    return acc
  }, [])
  const filiereGradient = `conic-gradient(${filiereStops.map((s) => `${s.color} ${s.start}deg ${s.end}deg`).join(',') || '#ede9fe 0deg 360deg'})`

  const byTeacher = useMemo(() => planned.reduce((a, s) => {
    const p = formatProf(s.president)
    if (p !== '-') a[p] = (a[p] || 0) + 1
    ;(s.jurys || []).forEach((j) => {
      const n = formatProf(j)
      if (n !== '-') a[n] = (a[n] || 0) + 1
    })
    return a
  }, {}), [planned])
  const teacherTop = Object.entries(byTeacher).sort((a, b) => b[1] - a[1]).slice(0, 10)
  const maxTeacher = Math.max(1, ...teacherTop.map(([, c]) => c))

  const juryFlows = useMemo(() => {
    const byProfessor = {}
    planned.forEach((item) => {
      const room = normalizeRoom(item?.salle?.nom)
      if (!room || room === '-') return
      const hour = parseHourNumber(item.heureDebut)
      const when = `${item.date || ''} ${String(hour ?? 0).padStart(2, '0')}`
      const members = [item.president, ...(item.jurys || [])]
      const uniqueMembers = new Map()
      members.forEach((member) => {
        const name = formatProf(member)
        if (name !== '-') {
          uniqueMembers.set(name.toLowerCase(), name)
        }
      })
      uniqueMembers.forEach((name) => {
        if (!byProfessor[name]) byProfessor[name] = []
        byProfessor[name].push({ when, room })
      })
    })

    return Object.entries(byProfessor)
      .map(([teacher, events]) => {
        const ordered = [...events].sort((a, b) => a.when.localeCompare(b.when))
        const rooms = []
        ordered.forEach((event) => {
          if (rooms.length === 0 || rooms[rooms.length - 1] !== event.room) {
            rooms.push(event.room)
          }
        })
        return { teacher, rooms, moves: Math.max(0, rooms.length - 1) }
      })
      .filter((flow) => flow.rooms.length > 1)
      .sort((a, b) => b.moves - a.moves || a.teacher.localeCompare(b.teacher))
      .slice(0, 12)
  }, [planned])

  const heatmapHours = useMemo(() => {
    const set = new Set()
    planned.forEach((item) => {
      const hour = parseHourNumber(item.heureDebut)
      if (hour !== null) set.add(hour)
    })
    const sorted = Array.from(set).sort((a, b) => a - b)
    return sorted.length > 0 ? sorted : [8, 9, 10, 11, 12, 13, 14, 15, 16, 17]
  }, [planned])

  const heatmapRows = useMemo(() => {
    const matrix = {}
    planned.forEach((item) => {
      const room = normalizeRoom(item?.salle?.nom)
      const hour = parseHourNumber(item.heureDebut)
      if (hour === null) return
      if (!matrix[room]) matrix[room] = {}
      matrix[room][hour] = (matrix[room][hour] || 0) + 1
    })
    return Object.entries(matrix).sort((a, b) => a[0].localeCompare(b[0]))
  }, [planned])
  const heatmapMax = Math.max(1, ...heatmapRows.flatMap(([, row]) => Object.values(row)))

  if (hasData === false) {
    return (
      <div className="page">
        <div className="empty-state">
          <h2 className="empty-state__title">Aucune donnee disponible</h2>
          <NavLink className="btn btn-primary" to="/import">Importer les donnees</NavLink>
        </div>
      </div>
    )
  }
  if (hasData === null) {
    return (
      <div className="page">
        <div className="empty-state"><h2 className="empty-state__title">Chargement...</h2></div>
      </div>
    )
  }

  const DAY_OPTIONS = [
    { value: 'MONDAY', label: 'LUN' },
    { value: 'TUESDAY', label: 'MAR' },
    { value: 'WEDNESDAY', label: 'MER' },
    { value: 'THURSDAY', label: 'JEU' },
    { value: 'FRIDAY', label: 'VEN' },
    { value: 'SATURDAY', label: 'SAM' },
    { value: 'SUNDAY', label: 'DIM' }
  ];

  return (
    <div className="page">
      <div className="page-header">
        <h1>Génération du planning</h1>
        <p style={{ color: '#64748b' }}>Configurez les options ci-dessous pour lancer l'algorithme de planification.</p>
      </div>

      <section className="section">
        <form className="card form-card card-static" onSubmit={handleGenerate} style={{ padding: '30px' }}>
          
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '20px', marginBottom: '20px' }}>
            <label className="form-label">
              Date de début
              <input type="date" style={{ marginTop: '8px' }} value={startDate} onChange={(e) => setStartDate(e.target.value)} required />
            </label>
            <label className="form-label">
              Date de fin
              <input type="date" style={{ marginTop: '8px' }} value={endDate} onChange={(e) => setEndDate(e.target.value)} required />
            </label>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '20px', marginBottom: '20px' }}>
            <label className="form-label">
              Heure de début (H)
              <select style={{ marginTop: '8px', padding: '8px', borderRadius: '6px', border: '1px solid #cbd5e1', width: '100%' }} value={startHour} onChange={(e) => setStartHour(parseInt(e.target.value))}>
                {Array.from({length: 12}, (_, i) => i + 7).map(h => <option key={h} value={h}>{h}:00</option>)}
              </select>
            </label>
            <label className="form-label">
              Heure de fin (H)
              <select style={{ marginTop: '8px', padding: '8px', borderRadius: '6px', border: '1px solid #cbd5e1', width: '100%' }} value={endHour} onChange={(e) => setEndHour(parseInt(e.target.value))}>
                {Array.from({length: 13}, (_, i) => i + 8).map(h => <option key={h} value={h}>{h}:00</option>)}
              </select>
            </label>
            <label className="form-label">
              Max. soutenances par créneau
              <select style={{ marginTop: '8px', padding: '8px', borderRadius: '6px', border: '1px solid #cbd5e1', width: '100%' }} value={maxSoutenancesParCreneau} onChange={(e) => setMaxSoutenancesParCreneau(parseInt(e.target.value))}>
                {[1, 2, 3, 4, 5, 6].map(n => <option key={n} value={n}>{n}</option>)}
              </select>
            </label>
          </div>

          <div style={{ marginBottom: '20px' }}>
            <label className="form-label">Jours de la semaine</label>
            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginTop: '8px' }}>
              {DAY_OPTIONS.map((day) => {
                const isSelected = daysOfWeek.includes(day.value);
                return (
                  <button 
                    key={day.value}
                    type="button"
                    onClick={() => handleDayToggle(day.value)}
                    style={{
                      padding: '8px 16px',
                      borderRadius: '20px',
                      border: isSelected ? '1px solid #2563eb' : '1px solid #e2e8f0',
                      backgroundColor: isSelected ? '#eff6ff' : '#f8fafc',
                      color: isSelected ? '#1d4ed8' : '#64748b',
                      fontWeight: isSelected ? '600' : '400',
                      cursor: 'pointer',
                      transition: 'all 0.2s ease'
                    }}
                  >
                    {day.label}
                  </button>
                )
              })}
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '20px', padding: '20px', backgroundColor: '#f8fafc', borderRadius: '8px', marginBottom: '20px' }}>
            <label className="form-label" style={{ margin: 0 }}>
              Dates spécifiques (Optionnel)
              <input type="text" style={{ marginTop: '8px' }} placeholder="Ex: 2024-06-01, 2024-06-05" value={specificDatesStr} onChange={(e) => setSpecificDatesStr(e.target.value)} />
            </label>

            <label className="form-label" style={{ margin: 0 }}>
              Heures spécifiques autorisées (Optionnel)
              <input type="text" style={{ marginTop: '8px' }} placeholder="Ex: 9, 10, 14, 15" value={allowedHoursStr} onChange={(e) => setAllowedHoursStr(e.target.value)} />
            </label>
          </div>

          <div className="form-actions" style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
            <button className="btn btn-primary" style={{ padding: '10px 24px', fontSize: '15px' }} type="submit" disabled={loading}>
              <UiIcon name="add" size={18} /> {loading ? 'Génération en cours...' : 'Générer le planning'}
            </button>
            {error && <span className="inline-error">{error}</span>}
            {result && <span style={{ color: '#166534', fontWeight: '500' }}>{result.message}</span>}
          </div>
        </form>
      </section>

      <section className="section">
        <h2 className="section-title"><UiIcon name="dashboard" size={20} />Répartition des Soutenances par Jour</h2>
        <div className="card card-static">
          {Object.keys(byDay).length === 0 ? <p className="muted">Aucune donnée</p> : Object.entries(byDay).map(([d, c]) => (
            <div key={d} style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>{d}</span><strong>{c}</strong></div>
              <div className="progress-bar"><div className="progress-bar__fill" style={{ width: `${(c / maxDay) * 100}%`, background: '#8ecae6' }} /></div>
            </div>
          ))}
        </div>
      </section>

      <section className="section">
        <h2 className="section-title"><UiIcon name="room" size={20} />Occupation des Salles</h2>
        <div className="card card-static" style={{ display: 'grid', gridTemplateColumns: '220px 1fr', gap: 20, alignItems: 'center' }}>
          <div style={{ width: 190, height: 190, borderRadius: '50%', background: pieGradient, margin: '0 auto' }} />
          <div className="tag-grid">{roomEntries.map(([r, c], i) => <span className="tag" key={r} style={{ background: ['#8ecae6', '#90be6d', '#f9c74f', '#f9844a', '#577590'][i % 5], color: '#0f172a' }}>{r} - {Math.round((c / totalRoom) * 100)}%</span>)}</div>
        </div>
      </section>

      <section className="section">
        <h2 className="section-title"><UiIcon name="filiere" size={20} />Répartition par Filière / Département</h2>
        <div className="card card-static" style={{ display: 'grid', gridTemplateColumns: '220px 1fr', gap: 20, alignItems: 'center' }}>
          <div style={{ width: 190, height: 190, borderRadius: '50%', background: filiereGradient, margin: '0 auto' }} />
          <div className="tag-grid">
            {filiereEntries.map(([name, count], i) => (
              <span className="tag" key={name} style={{ background: ['#c4b5fd', '#86efac', '#93c5fd', '#fcd34d', '#fca5a5', '#67e8f9'][i % 6], color: '#0f172a' }}>
                {name} - {Math.round((count / totalFiliere) * 100)}%
              </span>
            ))}
          </div>
        </div>
      </section>

      <section className="section">
        <h2 className="section-title"><UiIcon name="professor" size={20} />Charge de Travail des Enseignants</h2>
        <div className="card card-static">
          {teacherTop.length === 0 ? <p className="muted">Aucune donnée</p> : teacherTop.map(([name, c]) => (
            <div key={name} style={{ marginBottom: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between' }}><span>{name}</span><strong>{c}</strong></div>
              <div className="progress-bar"><div className="progress-bar__fill" style={{ width: `${(c / maxTeacher) * 100}%`, background: '#90be6d' }} /></div>
            </div>
          ))}
        </div>
      </section>

      <section className="section">
        <h2 className="section-title"><UiIcon name="flow" size={20} />Flux de Déplacement des Jurys</h2>
        <div className="card card-static">
          {juryFlows.length === 0 ? (
            <p className="muted">Aucun déplacement de jury détecté sur le planning actuel.</p>
          ) : (
            <div className="jury-flow-list">
              {juryFlows.map((flow) => (
                <div key={flow.teacher} className="jury-flow-item">
                  <div className="jury-flow-teacher">{flow.teacher}</div>
                  <div className="jury-flow-track">
                    {flow.rooms.map((room, index) => (
                      <div key={`${flow.teacher}-${room}-${index}`} className="jury-flow-node-wrap">
                        <span className="jury-flow-node">{room}</span>
                        {index < flow.rooms.length - 1 && <UiIcon name="arrowRight" size={14} className="jury-flow-arrow" />}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </section>

      <section className="section">
        <h2 className="section-title"><UiIcon name="heatmap" size={20} />Heatmap des Salles</h2>
        <div className="card card-static">
          {heatmapRows.length === 0 ? (
            <p className="muted">Aucune soutenance planifiée pour afficher la heatmap.</p>
          ) : (
            <div className="heatmap-wrap">
              <div className="heatmap-grid" style={{ gridTemplateColumns: `180px repeat(${heatmapHours.length}, minmax(34px, 1fr))` }}>
                <div className="heatmap-header-cell heatmap-header-room">Salle</div>
                {heatmapHours.map((hour) => <div key={`h-${hour}`} className="heatmap-header-cell">{String(hour).padStart(2, '0')}</div>)}
                {heatmapRows.map(([room, row]) => (
                  <div key={room} className="heatmap-row-group" style={{ display: 'contents' }}>
                    <div className="heatmap-room-cell">{room}</div>
                    {heatmapHours.map((hour) => {
                      const count = row[hour] || 0
                      const intensity = count === 0 ? 0 : count / heatmapMax
                      const bg = count === 0 ? '#eef2f7' : `rgba(10, 107, 94, ${0.18 + intensity * 0.72})`
                      return (
                        <div
                          key={`${room}-${hour}`}
                          className="heatmap-cell"
                          style={{ background: bg }}
                          title={`${room} - ${String(hour).padStart(2, '0')}:00 => ${count}`}
                        >
                          {count > 0 ? count : ''}
                        </div>
                      )
                    })}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </section>

      <section className="section">
        <div className="section-header"><h2>Planning final</h2></div>
        <div className="card table-card card-static">
          <div className="table-wrapper">
            <table>
              <thead><tr><th>Étudiant</th><th>Encadrant</th><th>Jury 1</th><th>Jury 2</th><th>Salle</th><th>Date</th><th>Heure</th></tr></thead>
              <tbody>
                {planned.map((item) => (
                  <tr key={item.id}>
                    <td>{(item.etudiants || []).map((e) => `${e.nom || ''} ${e.prenom || ''}`.trim()).join(' | ') || '-'}</td>
                    <td>{formatProf(item.president)}</td>
                    <td>{formatProf(item.jurys?.[0])}</td>
                    <td>{formatProf(item.jurys?.[1])}</td>
                    <td>{normalizeRoom(item?.salle?.nom)}</td>
                    <td>{item.date || '-'}</td>
                    <td>{formatHour(item.heureDebut)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </section>
    </div>
  )
}

export default PlanningPage
