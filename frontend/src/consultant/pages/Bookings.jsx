import { useEffect, useState } from 'react'
import { getConsultantBookings, acceptBooking, rejectBooking, completeBooking, getUsers, getServices } from '../../shared/lib/api'
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

function RejectModal({ onConfirm, onCancel }) {
  const [reason, setReason] = useState('')

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60">
      <div className="bg-[#1F2023] border border-[#2e303a] rounded-2xl w-full max-w-md mx-4 p-6 space-y-4">
        <h3 className="text-lg font-semibold text-white">Reject Booking</h3>
        <p className="text-sm text-gray-400">Optionally provide a reason for the client.</p>
        <textarea
          value={reason}
          onChange={(e) => setReason(e.target.value)}
          maxLength={500}
          rows={4}
          placeholder="Reason for rejection (optional)…"
          className="w-full bg-[#16171d] border border-[#3a3c48] rounded-xl px-3 py-2 text-sm text-gray-200 placeholder-gray-600 resize-none focus:outline-none focus:ring-2 focus:ring-red-500"
        />
        <p className="text-xs text-gray-600 text-right">{reason.length}/500</p>
        <div className="flex justify-end gap-3">
          <button
            onClick={onCancel}
            className="text-sm bg-[#2e303a] hover:bg-[#3a3c48] text-gray-300 px-4 py-2 rounded-lg transition-colors"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(reason.trim())}
            className="text-sm bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-lg transition-colors"
          >
            Confirm Reject
          </button>
        </div>
      </div>
    </div>
  )
}

export default function ConsultantBookings() {
  const consultantId = getUserId()
  const [bookings, setBookings] = useState([])
  const [usersMap, setUsersMap] = useState({})
  const [servicesMap, setServicesMap] = useState({})
  const [filter, setFilter] = useState('ACTIVE')
  const [loading, setLoading] = useState(true)
  const [rejectTarget, setRejectTarget] = useState(null)

  const load = async () => {
    setLoading(true)
    try {
      const [bookingsRes, usersRes, servicesRes] = await Promise.all([
        getConsultantBookings(consultantId),
        getUsers(),
        getServices(),
      ])
      const sorted = [...bookingsRes.data].sort((a, b) => new Date(b.requestedStartAt) - new Date(a.requestedStartAt))
      setBookings(sorted)
      const uMap = {}
      for (const u of usersRes.data) uMap[u.id] = u
      setUsersMap(uMap)
      const sMap = {}
      for (const s of servicesRes.data) sMap[s.id] = s
      setServicesMap(sMap)
    } catch {
      setBookings([])
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  const action = async (fn, bookingId, ...args) => {
    try {
      await fn(consultantId, bookingId, ...args)
      load()
    } catch (err) {
      alert(err.response?.data?.message ?? 'Action failed')
    }
  }

  const handleRejectConfirm = async (reason) => {
    const bookingId = rejectTarget
    setRejectTarget(null)
    await action(rejectBooking, bookingId, reason)
  }

  const clientName = (id) => {
    const u = usersMap[id]
    if (!u) return 'Client'
    return u.firstName && u.lastName ? `${u.firstName} ${u.lastName}` : u.email ?? 'Client'
  }

  const serviceName = (id) => servicesMap[id]?.title ?? 'Consulting Session'
  const filtered = filter === 'ACTIVE'
    ? [...bookings].filter((b) => ACTIVE_STATUSES.includes(b.status)).sort((a, b) => new Date(a.requestedStartAt) - new Date(b.requestedStartAt))
    : filter === 'ALL'
    ? bookings
    : bookings.filter((b) => b.status === filter)

  return (
    <div className="p-6 max-w-4xl mx-auto">
      {rejectTarget !== null && (
        <RejectModal
          onConfirm={handleRejectConfirm}
          onCancel={() => setRejectTarget(null)}
        />
      )}

      <h2 className="text-2xl font-semibold text-white mb-4">Booking Requests</h2>

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

      {loading ? (
        <div className="text-center text-gray-500 py-8">Loading…</div>
      ) : filtered.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          {bookings.length === 0 ? (
            'No bookings found.'
          ) : filter === 'ACTIVE' ? (
            <>No active bookings. <button className="text-indigo-400 hover:underline" onClick={() => setFilter('ALL')}>View all bookings</button></>
          ) : (
            'No bookings match this filter.'
          )}
        </div>
      ) : (
        <div className="space-y-3">
          {filtered.map((b) => (
            <div key={b.id} className="bg-[#1F2023] rounded-xl border border-[#2e303a] px-5 py-4">
              <div className="flex items-start justify-between">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-200">{serviceName(b.serviceId)}</span>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[b.status] ?? 'bg-gray-500/20 text-gray-400'}`}>
                      {b.status}
                    </span>
                  </div>
                  <p className="text-sm text-gray-400">from {clientName(b.clientId)}</p>
                  <p className="text-xs text-gray-600">
                    {b.requestedStartAt ? new Date(b.requestedStartAt).toLocaleString() : '—'}
                  </p>
                  {b.rejectionReason && (
                    <p className="text-xs text-red-400 mt-1">Reason: {b.rejectionReason}</p>
                  )}
                </div>

                <div className="flex gap-2">
                  {b.status === 'REQUESTED' && (
                    <>
                      <button
                        onClick={() => action(acceptBooking, b.id)}
                        className="text-sm bg-blue-600 hover:bg-blue-700 text-white px-3 py-1.5 rounded-lg transition-colors"
                      >
                        Accept
                      </button>
                      <button
                        onClick={() => setRejectTarget(b.id)}
                        className="text-sm bg-red-500/10 hover:bg-red-500/20 text-red-400 px-3 py-1.5 rounded-lg transition-colors"
                      >
                        Reject
                      </button>
                    </>
                  )}
                  {b.status === 'PAID' && (
                    <button
                      onClick={() => action(completeBooking, b.id)}
                      className="text-sm bg-green-600 hover:bg-green-700 text-white px-3 py-1.5 rounded-lg transition-colors"
                    >
                      Mark Complete
                    </button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
