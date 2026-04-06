import { jwtDecode } from 'jwt-decode'

export function saveAuth(token) {
  localStorage.setItem('token', token)
  try {
    const decoded = jwtDecode(token)
    localStorage.setItem('user', JSON.stringify(decoded))
  } catch {
    // ignore decode errors
  }
}

export function getToken() {
  return localStorage.getItem('token')
}

export function getUser() {
  try {
    return JSON.parse(localStorage.getItem('user'))
  } catch {
    return null
  }
}

export function getUserId() {
  return getUser()?.userId ?? getUser()?.sub
}

export function getRoles() {
  const user = getUser()
  if (!user) return []
  const raw = Array.isArray(user.roles)
    ? user.roles
    : typeof user.roles === 'string'
    ? user.roles.split(' ')
    : []
  // Strip Spring Security's "ROLE_" prefix so comparisons use plain role names
  return raw.map((r) => r.replace(/^ROLE_/i, ''))
}

export function hasRole(role) {
  return getRoles().some((r) => r.toUpperCase() === role.toUpperCase())
}

export function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
}

export function isLoggedIn() {
  const token = getToken()
  if (!token) return false
  try {
    const { exp } = jwtDecode(token)
    return Date.now() < exp * 1000
  } catch {
    return false
  }
}
