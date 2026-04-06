import { Navigate } from 'react-router-dom'
import { useAuth } from '../../auth/context/AuthContext'

export default function ProtectedRoute({ children, role }) {
  const { isLoggedIn, roles } = useAuth()

  if (!isLoggedIn) return <Navigate to="/login" replace />

  if (role && !roles.some((r) => r.toUpperCase() === role.toUpperCase())) {
    return <Navigate to="/unauthorized" replace />
  }

  return children
}
