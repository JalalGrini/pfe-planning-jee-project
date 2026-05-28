const getDefaultApiBaseUrl = () => {
  const contextPath = window.location.pathname.split('/').filter(Boolean)[0]
  return contextPath
    ? `${window.location.origin}/${contextPath}/api`
    : `${window.location.origin}/api`
}

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || getDefaultApiBaseUrl()

const buildUrl = (path) => `${API_BASE_URL}${path}`

const parseResponse = async (response) => {
  const contentType = response.headers.get('content-type') || ''
  if (!response.ok) {
    if (contentType.includes('application/json')) {
      const data = await response.json()
      throw new Error(data.error || data.message || 'Erreur serveur')
    }
    const text = await response.text()
    throw new Error(text || 'Erreur serveur')
  }
  if (contentType.includes('application/json')) {
    return response.json()
  }
  return response.text()
}

export const getStats = async () => {
  const response = await fetch(buildUrl('/statistiques'))
  return parseResponse(response)
}

export const importSoutenances = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const response = await fetch(buildUrl('/soutenances/importer'), {
    method: 'POST',
    body: formData,
  })
  return parseResponse(response)
}

export const importMultipleFiles = async (files) => {
  const formData = new FormData()
  if (Array.isArray(files.etudiants)) {
    files.etudiants.forEach((file) => formData.append('etudiants', file))
  } else if (files.etudiants) {
    formData.append('etudiants', files.etudiants)
  }
  if (files.professeurs) formData.append('professeurs', files.professeurs)
  if (files.salles) formData.append('salles', files.salles)
  const response = await fetch(buildUrl('/soutenances/importer-multiple'), {
    method: 'POST',
    body: formData,
  })
  return parseResponse(response)
}

export const importComplet = async (file) => {
  const formData = new FormData()
  formData.append('file', file)
  const response = await fetch(buildUrl('/soutenances/importer-complet'), {
    method: 'POST',
    body: formData,
  })
  return parseResponse(response)
}

export const getProfesseurs = async () => {
  const response = await fetch(buildUrl('/professeurs'))
  return parseResponse(response)
}

export const getIndisponibilites = async (profId) => {
  const response = await fetch(buildUrl(`/professeurs/${profId}/indisponibilites`))
  return parseResponse(response)
}

export const addIndisponibilite = async (profId, payload) => {
  const response = await fetch(buildUrl(`/professeurs/${profId}/indisponibilites`), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return parseResponse(response)
}

export const deleteIndisponibilite = async (id) => {
  const response = await fetch(buildUrl(`/professeurs/indisponibilites/${id}`), {
    method: 'DELETE',
  })
  return parseResponse(response)
}

export const getBinomes = async () => {
  const response = await fetch(buildUrl('/binomes'))
  return parseResponse(response)
}

export const confirmerBinome = async (payload) => {
  const response = await fetch(buildUrl('/binomes/confirmer'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return parseResponse(response)
}

export const generatePlanning = async (payload) => {
  const response = await fetch(buildUrl('/planning/generer'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return parseResponse(response)
}

export const validatePlanning = async () => {
  const response = await fetch(buildUrl('/planning/valider'))
  return parseResponse(response)
}

export const listSoutenances = async (filter) => {
  const query = filter ? `?filter=${encodeURIComponent(filter)}` : ''
  const response = await fetch(buildUrl(`/soutenances${query}`))
  return parseResponse(response)
}

export const searchByEncadrant = async (query) => {
  const q = query ? `?q=${encodeURIComponent(query)}` : ''
  const response = await fetch(buildUrl(`/soutenances/par-encadrant${q}`))
  return parseResponse(response)
}

export const downloadEvaluation = async (id) => {
  const response = await fetch(buildUrl(`/soutenances/${id}/evaluation`))
  if (!response.ok) {
    throw new Error('Erreur lors du telechargement du PV')
  }
  return response.blob()
}

export const downloadExport = async (type) => {
  const response = await fetch(buildUrl(`/export/${type}`))
  if (!response.ok) {
    if (response.status === 409) {
      throw new Error('Vous devez générer le planning avant de télécharger les exports.')
    }
    throw new Error("Erreur lors de la generation de l'export")
  }
  return response.blob()
}

export const saveBlob = (blob, filename) => {
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = filename
  document.body.appendChild(link)
  link.click()
  link.remove()
  window.URL.revokeObjectURL(url)
}

export const getEtudiantsDisponibles = async () => {
  const response = await fetch(buildUrl('/binomes/etudiants'))
  return parseResponse(response)
}

export const creerBinomeManuel = async (payload) => {
  const response = await fetch(buildUrl('/binomes/manual'), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
  return parseResponse(response)
}
