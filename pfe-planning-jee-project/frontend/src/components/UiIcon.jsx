const ICONS = {
  upload: (
    <>
      <path d="M12 15V4" />
      <path d="m8 8 4-4 4 4" />
      <path d="M4 16v2a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2v-2" />
    </>
  ),
  planning: (
    <>
      <rect x="3" y="5" width="18" height="16" rx="2" />
      <path d="M16 3v4M8 3v4M3 10h18" />
      <path d="M13 14h3v3" />
      <path d="m16 14-3 3" />
    </>
  ),
  dashboard: (
    <>
      <path d="M3 3v18h18" />
      <rect x="6" y="12" width="3" height="6" rx="1" />
      <rect x="11" y="9" width="3" height="9" rx="1" />
      <rect x="16" y="6" width="3" height="12" rx="1" />
    </>
  ),
  students: (
    <>
      <path d="M16 21v-2a4 4 0 0 0-4-4H7a4 4 0 0 0-4 4v2" />
      <circle cx="9.5" cy="7" r="3.5" />
      <path d="M20 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a3.5 3.5 0 0 1 0 6.74" />
    </>
  ),
  professor: (
    <>
      <circle cx="12" cy="7" r="4" />
      <path d="M4 21a8 8 0 0 1 16 0" />
      <path d="M18.5 13.5 20 15l-1.5 1.5" />
      <path d="M15.5 15h4.5" />
    </>
  ),
  room: (
    <>
      <rect x="3" y="4" width="18" height="16" rx="2" />
      <path d="M8 20v-4h8v4M7 9h2m6 0h2m-10 4h2m6 0h2" />
    </>
  ),
  filiere: (
    <>
      <path d="M12 3 3 8l9 5 9-5-9-5Z" />
      <path d="m3 12 9 5 9-5" />
      <path d="m3 16 9 5 9-5" />
    </>
  ),
  file: (
    <>
      <path d="M14 2H7a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7Z" />
      <path d="M14 2v5h5" />
    </>
  ),
  excel: (
    <>
      <rect x="3" y="4" width="18" height="16" rx="2" />
      <path d="M9 4v16M3 10h18M3 14h18" />
    </>
  ),
  pdf: (
    <>
      <path d="M14 2H7a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7Z" />
      <path d="M14 2v5h5M8 15h8M8 11h4" />
    </>
  ),
  timeline: (
    <>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7v5l3 2" />
    </>
  ),
  heatmap: (
    <>
      <rect x="4" y="4" width="4" height="4" rx="1" />
      <rect x="10" y="4" width="4" height="4" rx="1" />
      <rect x="16" y="4" width="4" height="4" rx="1" />
      <rect x="4" y="10" width="4" height="4" rx="1" />
      <rect x="10" y="10" width="4" height="4" rx="1" />
      <rect x="16" y="10" width="4" height="4" rx="1" />
      <rect x="4" y="16" width="4" height="4" rx="1" />
      <rect x="10" y="16" width="4" height="4" rx="1" />
      <rect x="16" y="16" width="4" height="4" rx="1" />
    </>
  ),
  flow: (
    <>
      <rect x="2" y="5" width="6" height="6" rx="1.5" />
      <rect x="16" y="5" width="6" height="6" rx="1.5" />
      <rect x="9" y="13" width="6" height="6" rx="1.5" />
      <path d="M8 8h6m0 0-1.8-1.8M14 8l-1.8 1.8M12 11v2" />
    </>
  ),
  arrowRight: (
    <>
      <path d="M5 12h14" />
      <path d="m13 6 6 6-6 6" />
    </>
  ),
  chat: (
    <>
      <path d="M21 12a8.5 8.5 0 0 1-8.5 8.5H5l-3 2 1.2-4.2A8.5 8.5 0 1 1 21 12Z" />
      <path d="M8 12h8M8 9h5" />
    </>
  ),
  send: (
    <>
      <path d="M22 2 11 13" />
      <path d="m22 2-7 20-4-9-9-4Z" />
    </>
  ),
  close: (
    <>
      <path d="M18 6 6 18M6 6l12 12" />
    </>
  ),
}

function UiIcon({ name, size = 22, className = '', title = '' }) {
  const body = ICONS[name] || ICONS.file
  return (
    <svg
      viewBox="0 0 24 24"
      width={size}
      height={size}
      className={`icon-svg ${className}`.trim()}
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden={title ? undefined : 'true'}
      role={title ? 'img' : 'presentation'}
    >
      {title ? <title>{title}</title> : null}
      {body}
    </svg>
  )
}

export default UiIcon
