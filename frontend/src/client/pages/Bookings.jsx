import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getClientBookings, cancelBooking, getUsers, getServices, getPolicy, getPaymentHistory } from '../../shared/lib/api'
import { getUserId } from '../../shared/lib/auth'

const STATUS_COLORS = {
  REQUESTED: 'bg-yellow-500/20 text-yellow-400',
  CONFIRMED: 'bg-blue-500/20 text-blue-400',
  PAID: 'bg-green-500/20 text-green-400',
  COMPLETED: 'bg-gray-500/20 text-gray-400',
  REJECTED: 'bg-red-500/20 text-red-400',
  CANCELLED: 'bg-red-500/10 text-red-400',
}

const ACTIVE_STATUSES = ['REQUESTED', 'CONFIRMED', 'PAID']
const FILTERS = ['ALL', 'REQUESTED', 'CONFIRMED', 'PAID', 'COMPLETED', 'REJECTED', 'CANCELLED']

export default function ClientBookings() {
  const [bookings, setBookings] = useState([])
  const [usersMap, setUsersMap] = useState({})
  const [servicesMap, setServicesMap] = useState({})
  const [paymentsMap, setPaymentsMap] = useState({})
  const [refundPolicy, setRefundPolicy] = useState(null)
  const [filter, setFilter] = useState('ACTIVE')
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  const load = async () => {
    const clientId = getUserId()
    try {
      const [bookingsRes, usersRes, servicesRes, policyRes, paymentsRes] = await Promise.all([
        getClientBookings(clientId),
        getUsers(),
        getServices(),
        getPolicy('REFUND_POLICY').catch(() => ({ data: null })),
        getPaymentHistory(clientId).catch(() => ({ data: [] }))
      ])
      const sorted = [...bookingsRes.data].sort((a, b) => new Date(b.requestedStartAt) - new Date(a.requestedStartAt))
      setBookings(sorted)
      const uMap = {}
      for (const u of usersRes.data) uMap[u.id] = u
      setUsersMap(uMap)
      const sMap = {}
      for (const s of servicesRes.data) sMap[s.id] = s
      setServicesMap(sMap)
      const pMap = {}
      for (const p of (paymentsRes.data || [])) {
        if (p.bookingId) pMap[p.bookingId] = p
      }
      setPaymentsMap(pMap)
      
      if (policyRes.data && policyRes.data.policyValue) {
        setRefundPolicy(JSON.parse(policyRes.data.policyValue))
      }
    } catch {
      setBookings([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const getRefundEstimate = (booking) => {
    if (!refundPolicy || !refundPolicy.tiers || !booking.requestedStartAt) return 100

    const hoursUntilStart = (new Date(booking.requestedStartAt) - new Date()) / (1000 * 60 * 60)
    let refundPercentage = 0

    const applicableTiers = refundPolicy.tiers.filter(tier => hoursUntilStart >= tier.hoursBefore)
    if (applicableTiers.length > 0) {
      const bestTier = applicableTiers.reduce((max, tier) => 
        tier.hoursBefore > max.hoursBefore ? tier : max
      )
      refundPercentage = bestTier.refundPercentage * 100
    }

    return refundPercentage
  }

  const handleCancel = async (booking) => {
    const refundEstimate = booking.status === 'PAID' ? getRefundEstimate(booking) : 100
    const msg = booking.status === 'PAID' ? `Cancel this booking? Based on our policy, you will receive a ${refundEstimate}% refund for this paid booking.` : 'Cancel this booking?'
    
    if (!confirm(msg)) return
    try {
      await cancelBooking(booking.id)
      load()
    } catch (err) {
      alert(err.response?.data?.message ?? 'Could not cancel booking')
    }
  }

  const consultantName = (id) => {
    const u = usersMap[id]
    if (!u) return 'Consultant'
    return u.firstName && u.lastName ? `${u.firstName} ${u.lastName}` : u.email ?? 'Consultant'
  }

  const serviceName = (id) => servicesMap[id]?.title ?? 'Consulting Session'

  if (loading) return <div className="p-8 text-center text-gray-500">Loading…</div>

  const filtered = filter === 'ACTIVE'
    ? [...bookings].filter((b) => ACTIVE_STATUSES.includes(b.status)).sort((a, b) => new Date(a.requestedStartAt) - new Date(b.requestedStartAt))
    : filter === 'ALL'
    ? bookings
    : bookings.filter((b) => b.status === filter)

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <h2 className="text-2xl font-semibold text-white mb-4">My Bookings</h2>

      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => setFilter('ACTIVE')}
          className={`text-sm px-4 py-1.5 rounded-lg font-medium transition-colors ${
            filter === 'ACTIVE'
              ? 'bg-indigo-600 text-white'
              : 'bg-[#2e303a] text-gray-400 hover:bg-[#3a3c48] hover:text-gray-200'
          }`}
        >
          Active
        </button>
        <select
          value={filter === 'ACTIVE' ? '' : filter}
          onChange={(e) => setFilter(e.target.value || 'ACTIVE')}
          className="bg-[#2e303a] border border-[#444] text-sm text-gray-300 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 cursor-pointer"
        >
          <option value="">Filter by status…</option>
          {FILTERS.map((f) => (
            <option key={f} value={f}>{f.charAt(0) + f.slice(1).toLowerCase()}</option>
          ))}
        </select>
        {filter !== 'ACTIVE' && (
          <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${STATUS_COLORS[filter] ?? 'bg-gray-500/20 text-gray-400'}`}>
            {filter}
          </span>
        )}
      </div>

      {filtered.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          {bookings.length === 0 ? (
            <>No bookings yet.{' '}<button className="text-indigo-400 hover:underline" onClick={() => navigate('/client/services')}>Browse services</button></>
          ) : filter === 'ACTIVE' ? (
            <>No active bookings.{' '}<button className="text-indigo-400 hover:underline" onClick={() => setFilter('ALL')}>View all bookings</button></>
          ) : (
            'No bookings match this filter.'
          )}
        </div>
      )}

      {refundPolicy && refundPolicy.tiers && (
        <div className="mb-6 bg-[#1F2023] rounded-xl border border-[#2e303a] p-4 text-sm">
          <h3 className="font-semibold text-white mb-2">Refund Policy</h3>
          <ul className="text-gray-400 space-y-1">
            {refundPolicy.tiers.sort((a, b) => b.hoursBefore - a.hoursBefore).map((tier, idx) => (
              <li key={idx}>Cancel at least {tier.hoursBefore}h before: {tier.refundPercentage * 100}% refund</li>
            ))}
          </ul>
        </div>
      )}

      <div className="space-y-3">
        {filtered.map((b) => (
          <div key={b.id} className="bg-[#1F2023] rounded-xl border border-[#2e303a] px-5 py-4 flex items-center justify-between">
            <div className="space-y-1">
              <div className="flex items-center gap-2">
                <span className="font-medium text-gray-200">{serviceName(b.serviceId)}</span>
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[b.status] ?? 'bg-gray-500/20 text-gray-400'}`}>
                  {b.status}
                </span>
                {b.status === 'CANCELLED' && paymentsMap[b.id]?.refundAmount != null && (
                  <span className="text-xs text-gray-400">
                    Cancelled with {(paymentsMap[b.id].refundAmount / paymentsMap[b.id].amount * 100).toFixed(0)}% refund
                  </span>
                )}
              </div>
              <p className="text-sm text-gray-400">with {consultantName(b.consultantId)}</p>
              <p className="text-xs text-gray-600">
                {b.requestedStartAt ? new Date(b.requestedStartAt).toLocaleString() : '—'}
              </p>
              {b.rejectionReason && (
                <p className="text-xs text-red-400 mt-1">Reason: {b.rejectionReason}</p>
              )}
            </div>

            <div className="flex gap-2">
              {b.status === 'CONFIRMED' && (
                <button
                  onClick={() => navigate('/client/payments', { state: { booking: { ...b, serviceName: serviceName(b.serviceId), amount: servicesMap[b.serviceId]?.basePrice ?? 1 } } })}
                  className="text-sm bg-green-600 hover:bg-green-700 text-white px-3 py-1.5 rounded-lg transition-colors"
                >
                  Pay
                </button>
              )}
              {['REQUESTED', 'CONFIRMED', 'PAID'].includes(b.status) && (
                <button
                  onClick={() => handleCancel(b)}
                  className="text-sm bg-red-500/10 hover:bg-red-500/20 text-red-400 px-3 py-1.5 rounded-lg transition-colors"
                >
                  Cancel
                </button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}
