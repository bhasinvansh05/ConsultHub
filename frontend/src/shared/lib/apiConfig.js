/**
 * Production builds set VITE_API_BASE_URL to the backend origin (e.g. https://api.example.com).
 * Dev leaves it empty so Vite proxy (vite.config.js) can forward /api, /bookings, etc.
 */

/** @returns {string} Base URL with no trailing slash; empty string when unset. */
export function getApiBaseUrl() {
  return (import.meta.env.VITE_API_BASE_URL || '').replace(/\/+$/, '')
}

/**
 * @param {string} path - API path starting with / (e.g. /api/services)
 * @returns {string} Absolute URL when base is set, otherwise same path for proxy/same-origin
 */
export function resolveApiUrl(path) {
  const p = path.startsWith('/') ? path : `/${path}`
  const base = getApiBaseUrl()
  return base ? `${base}${p}` : p
}
