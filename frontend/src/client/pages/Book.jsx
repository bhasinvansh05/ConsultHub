import { useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { createBooking } from '../../shared/lib/api'
import { getUserId } from '../../shared/lib/auth'

export default function Book() {
  const { state } = useLocation()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const slot = state?.slot
  const consultant = state?.consultant
  const service = state?.service

  const consultantName = consultant
    ? `${consultant.firstName ?? ''} ${consultant.lastName ?? ''}`.trim() || consultant.email || 'Consultant'
    : 'Consultant'

  if (!slot) {
    return (
      <div className="p-8 text-center">
        <p className="text-gray-500">
          No slot selected.{' '}
          <button className="text-indigo-400 hover:underline" onClick={() => navigate('/client/services')}>
            Browse services
          </button>
        </p>
      </div>
    )
  }

  const handleBook = async () => {
    setError('')
    setLoading(true)
    try {
      const clientId = getUserId()
      await createBooking({ clientId: Number(clientId), slotId: slot.id })
      navigate('/client/bookings')
    } catch (err) {
      setError(err.response?.data?.message ?? 'Booking failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="p-6 max-w-md mx-auto">
      <button onClick={() => navigate(-1)} className="text-sm text-gray-500 hover:text-gray-300 transition-colors mb-4">
        ← Back
      </button>
      <div className="bg-[#1F2023] rounded-2xl border border-[#2e303a] p-6">
        <h2 className="text-xl font-semibold text-white mb-4">Confirm Booking</h2>

        <div className="space-y-3 mb-6">
          {service && (
            <div className="flex justify-between text-sm">
              <span className="text-gray-500">Service</span>
              <span className="text-gray-200 font-medium">{service.title}</span>
            </div>
          )}
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">Consultant</span>
            <span className="text-gray-200 font-medium">{consultantName}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">Start</span>
            <span className="text-gray-200">{new Date(slot.startAt).toLocaleString()}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span className="text-gray-500">End</span>
            <span className="text-gray-200">{new Date(slot.endAt).toLocaleString()}</span>
          </div>
          {service && (
            <div className="flex justify-between items-center text-sm border-t border-[#2e303a] pt-3">
              <span className="text-gray-500">Price</span>
              {service.originalPrice && Number(service.originalPrice) > Number(service.basePrice) ? (
                <div className="flex items-center gap-2">
                  <span className="text-gray-500 line-through">${Number(service.originalPrice).toFixed(2)}</span>
                  <span className="text-green-400 font-semibold">${Number(service.basePrice).toFixed(2)}</span>
                </div>
              ) : (
                <span className="text-gray-200 font-semibold">${Number(service.basePrice).toFixed(2)}</span>
              )}
            </div>
          )}
        </div>

        {error && (
          <div className="mb-4 text-sm text-red-400 bg-red-500/10 border border-red-500/30 rounded-lg px-3 py-2">
            {error}
          </div>
        )}

        <button
          onClick={handleBook}
          disabled={loading}
          className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2.5 rounded-lg text-sm transition-colors disabled:opacity-50"
        >
          {loading ? 'Booking…' : 'Confirm Booking'}
        </button>
      </div>
    </div>
  )
}
