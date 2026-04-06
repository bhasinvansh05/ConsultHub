import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../../auth/context/AuthContext'
import { getConsultantRegistrationStatus } from '../lib/api'
import { getUserId } from '../lib/auth'

export default function ConsultantRoute({ children }) {
  const { isLoggedIn, roles } = useAuth()
  const [status, setStatus] = useState(null) // null = loading

  const isConsultant = roles.some((r) => r.toUpperCase() === 'CONSULTANT')

  useEffect(() => {
    if (!isConsultant) return
    const id = getUserId()
    getConsultantRegistrationStatus(id)
      .then((r) => setStatus(r.data.status))
      .catch(() => setStatus('UNKNOWN'))
  }, [isConsultant])

  if (!isLoggedIn) return <Navigate to="/login" replace />
  if (!isConsultant) return <Navigate to="/unauthorized" replace />
  if (status === null) return <div className="p-8 text-center text-gray-500">Loading…</div>

  if (status === 'PENDING') {
    return (
      <div className="min-h-[60vh] flex items-center justify-center p-6">
        <div className="max-w-md w-full bg-[#1F2023] border border-[#2e303a] rounded-2xl p-8 text-center">
          <div className="w-14 h-14 rounded-full bg-yellow-500/10 flex items-center justify-center mx-auto mb-4">
            <svg className="w-7 h-7 text-yellow-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
          </div>
          <h2 className="text-xl font-semibold text-white mb-2">Application Under Review</h2>
          <p className="text-gray-400 text-sm leading-relaxed">
            Your consultant application has been submitted and is waiting for admin approval.
            You'll be able to access all consultant features once approved.
          </p>
          <p className="text-xs text-gray-600 mt-4">
            Please check back later or contact support if this takes too long.
          </p>
        </div>
      </div>
    )
  }

  if (status === 'REJECTED') {
    return (
      <div className="min-h-[60vh] flex items-center justify-center p-6">
        <div className="max-w-md w-full bg-[#1F2023] border border-red-500/20 rounded-2xl p-8 text-center">
          <div className="w-14 h-14 rounded-full bg-red-500/10 flex items-center justify-center mx-auto mb-4">
            <svg className="w-7 h-7 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2}
                d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
          <h2 className="text-xl font-semibold text-white mb-2">Application Not Approved</h2>
          <p className="text-gray-400 text-sm leading-relaxed">
            Unfortunately your consultant application was not approved. Please contact support for more information.
          </p>
        </div>
      </div>
    )
  }

  // APPROVED or NOT_FOUND (edge case) — allow through
  return children
}
