import { createContext, useContext, useState, useCallback } from 'react'
import { saveAuth, logout as doLogout, getUser, isLoggedIn, getRoles } from '../../shared/lib/auth'
import { login as apiLogin } from '../../shared/lib/api'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => (isLoggedIn() ? getUser() : null))

  const login = useCallback(async (email, password) => {
    const res = await apiLogin(email, password)
    const { token } = res.data
    saveAuth(token)
    const decoded = getUser()
    setUser(decoded)
    return decoded
  }, [])

  const logout = useCallback(() => {
    doLogout()
    setUser(null)
  }, [])

  const roles = user ? getRoles() : []

  return (
    <AuthContext.Provider value={{ user, login, logout, roles, isLoggedIn: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  return useContext(AuthContext)
}
