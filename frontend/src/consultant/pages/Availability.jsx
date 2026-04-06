import { useEffect, useMemo, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { createSlot, deleteSlot, getAvailability, getConsultantBookings, getServices, getUsers } from '../../shared/lib/api'
import { getUserId } from '../../shared/lib/auth'
import { Trash2 } from 'lucide-react'

const STATUS_COLORS = {
  REQUESTED: 'bg-yellow-500/20 text-yellow-400',
  CONFIRMED: 'bg-blue-500/20 text-blue-400',
  PAID: 'bg-green-500/20 text-green-400',
  COMPLETED: 'bg-gray-500/20 text-gray-400',
  REJECTED: 'bg-red-500/20 text-red-400',
  CANCELLED: 'bg-red-500/10 text-red-400',
}

const FILTERS = ['UPCOMING', 'REQUESTED', 'PAST', 'SLOTS']

function formatPerson(user) {
  if (!user) return 'Client'
  return user.firstName && user.lastName ? `${user.firstName} ${user.lastName}` : user.email ?? 'Client'
}

function formatDateTimeRange(startAt, endAt) {
  const start = new Date(startAt)
  const end = new Date(endAt)
  return `${start.toLocaleString()} - ${end.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}`
}

export default function ConsultantAvailability() {
  const consultantId = getUserId()
  const location = useLocation()
  const preselectedServiceId = location.state?.serviceId ?? ''

  const [slots, setSlots] = useState([])
  const [bookings, setBookings] = useState([])
  const [services, setServices] = useState([])
  const [servicesMap, setServicesMap] = useState({})
  const [usersMap, setUsersMap] = useState({})
  const [filter, setFilter] = useState('UPCOMING')
  const [showForm, setShowForm] = useState(Boolean(preselectedServiceId))
  const [form, setForm] = useState({ serviceId: preselectedServiceId, startAt: '' })
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  const load = async () => {
    setLoading(true)
    try {
      const [slotsRes, bookingsRes, servicesRes, usersRes] = await Promise.all([
        getAvailability(consultantId),
        getConsultantBookings(consultantId),
        getServices(),
        getUsers(),
      ])

      setSlots(slotsRes.data ?? [])
      setBookings(bookingsRes.data ?? [])
      setServices(servicesRes.data ?? [])

      const sMap = {}
      for (const s of servicesRes.data ?? []) sMap[s.id] = s
      setServicesMap(sMap)

      const uMap = {}
      for (const u of usersRes.data ?? []) uMap[u.id] = u
      setUsersMap(uMap)
    } catch {
      setSlots([])
      setBookings([])
      setServices([])
      setServicesMap({})
      setUsersMap({})
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [consultantId])

  const selectedService = services.find((service) => String(service.id) === String(form.serviceId))
  const durationMinutes = selectedService?.durationMinutes ?? null
  const computedEndAt = form.startAt && durationMinutes
    ? new Date(new Date(form.startAt).getTime() + durationMinutes * 60000)
    : null

  const { upcomingBookings, requestedBookings, pastBookings, openSlots } = useMemo(() => {
    const now = Date.now()

    const sortedBookings = [...bookings].sort(
      (a, b) => new Date(a.requestedStartAt) - new Date(b.requestedStartAt)
    )
    const sortedSlots = [...slots].sort(
      (a, b) => new Date(a.startAt) - new Date(b.startAt)
    )

    return {
      upcomingBookings: sortedBookings.filter((booking) => {
        const end = new Date(booking.requestedEndAt ?? booking.requestedStartAt).getTime()
        return end >= now && !['COMPLETED', 'REJECTED', 'CANCELLED'].includes(booking.status)
      }),
      requestedBookings: sortedBookings.filter((booking) => booking.status === 'REQUESTED'),
      pastBookings: sortedBookings.filter((booking) => {
        const end = new Date(booking.requestedEndAt ?? booking.requestedStartAt).getTime()
        return end < now || ['COMPLETED', 'REJECTED', 'CANCELLED'].includes(booking.status)
      }),
      openSlots: sortedSlots.filter((slot) => new Date(slot.endAt).getTime() >= now),
    }
  }, [bookings, slots])

  const counts = {
    UPCOMING: upcomingBookings.length,
    REQUESTED: requestedBookings.length,
    PAST: pastBookings.length,
    SLOTS: openSlots.length,
  }

  const currentItems = filter === 'UPCOMING'
    ? upcomingBookings
    : filter === 'REQUESTED'
      ? requestedBookings
      : filter === 'PAST'
        ? pastBookings
        : openSlots

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (!computedEndAt) {
      setError('Select a service and start time')
      return
    }

    setSaving(true)
    try {
      await createSlot(consultantId, {
        serviceId: Number.parseInt(form.serviceId, 10),
        startAt: new Date(form.startAt).toISOString(),
        endAt: computedEndAt.toISOString(),
      })
      setForm({ serviceId: '', startAt: '' })
      setShowForm(false)
      setFilter('SLOTS')
      await load()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to create slot')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (slotId) => {
    if (!confirm('Delete this slot?')) return
    try {
      await deleteSlot(consultantId, slotId)
      await load()
    } catch (err) {
      alert(err.response?.data?.message ?? 'Failed to delete slot')
    }
  }

  const serviceName = (id) => servicesMap[id]?.title ?? 'Consulting Session'

  return (
    <div className="p-6 max-w-4xl mx-auto">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-white">Availability</h2>
          <p className="text-sm text-gray-400 mt-1">Manage open slots and keep past sessions under one simple filter.</p>
        </div>
        <button
          onClick={() => setShowForm((v) => !v)}
          className="text-sm bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg transition-colors"
        >
          {showForm ? 'Cancel' : '+ Add Slot'}
        </button>
      </div>

      {showForm && (
        <form onSubmit={handleSubmit} className="bg-[#1F2023] rounded-xl border border-[#2e303a] p-5 mb-6 space-y-4">
          <h3 className="font-medium text-gray-200">New Availability Slot</h3>

          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Service</label>
            <select
              required
              value={form.serviceId}
              onChange={(e) => setForm((f) => ({ ...f, serviceId: e.target.value }))}
              className="w-full bg-[#16171d] border border-[#333333] rounded-lg px-3 py-2 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            >
              <option value="">Select a service...</option>
              {services.map((s) => (
                <option key={s.id} value={s.id}>{s.title}</option>
              ))}
            </select>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">Start</label>
              <input
                type="datetime-local"
                required
                value={form.startAt}
                onChange={(e) => setForm((f) => ({ ...f, startAt: e.target.value }))}
                className="w-full bg-[#16171d] border border-[#333333] rounded-lg px-3 py-2 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-300 mb-1">
                End <span className="text-gray-600 font-normal">(auto - {durationMinutes ? `${durationMinutes} min` : 'select service'})</span>
              </label>
              <div className="w-full bg-[#16171d] border border-[#333333] rounded-lg px-3 py-2 text-sm text-gray-500">
                {computedEndAt ? computedEndAt.toLocaleString() : '-'}
              </div>
            </div>
          </div>

          {error && (
            <p className="text-sm text-red-400 bg-red-500/10 border border-red-500/30 rounded-lg px-3 py-2">{error}</p>
          )}

          <button
            type="submit"
            disabled={saving}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 rounded-lg text-sm transition-colors disabled:opacity-50"
          >
            {saving ? 'Saving...' : 'Add Slot'}
          </button>
        </form>
      )}

      <div className="flex items-center gap-3 mb-6">
        <button
          onClick={() => setFilter('UPCOMING')}
          className={`text-sm px-4 py-1.5 rounded-lg font-medium transition-colors ${
            filter === 'UPCOMING'
              ? 'bg-indigo-600 text-white'
              : 'bg-[#2e303a] text-gray-400 hover:bg-[#3a3c48] hover:text-gray-200'
          }`}
        >
          Upcoming
        </button>
        <select
          value={filter === 'UPCOMING' ? '' : filter}
          onChange={(e) => setFilter(e.target.value || 'UPCOMING')}
          className="bg-[#2e303a] border border-[#444] text-sm text-gray-300 rounded-lg px-3 py-1.5 focus:outline-none focus:ring-2 focus:ring-indigo-500 cursor-pointer"
        >
          <option value="">More filters...</option>
          {FILTERS.filter((value) => value !== 'UPCOMING').map((value) => (
            <option key={value} value={value}>
              {value === 'REQUESTED' ? 'Requested' : value === 'PAST' ? 'Past' : 'Open Slots'}
            </option>
          ))}
        </select>
        <span className={`text-xs px-2.5 py-1 rounded-full font-medium ${
          filter === 'SLOTS'
            ? 'bg-indigo-500/20 text-indigo-300'
            : STATUS_COLORS[filter] ?? 'bg-gray-500/20 text-gray-400'
        }`}>
          {counts[filter]}
        </span>
      </div>

      {loading ? (
        <div className="text-center text-gray-500 py-8">Loading...</div>
      ) : currentItems.length === 0 ? (
        <p className="text-gray-500 text-sm">
          {filter === 'SLOTS' ? 'No open slots yet. Add one above.' : 'No items found for this filter.'}
        </p>
      ) : (
        <div className="space-y-3">
          {filter === 'SLOTS' ? (
            currentItems.map((slot) => (
              <div key={slot.id} className="group bg-[#1F2023] rounded-xl border border-[#2e303a] px-5 py-4 flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-200">{serviceName(slot.serviceId)}</p>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {formatDateTimeRange(slot.startAt, slot.endAt)}
                  </p>
                </div>
                <button
                  onClick={() => handleDelete(slot.id)}
                  className="opacity-0 group-hover:opacity-100 transition-opacity p-1.5 rounded-md text-red-400 hover:text-red-300 hover:bg-red-500/10"
                  title="Delete slot"
                >
                  <Trash2 className="w-4 h-4" />
                </button>
              </div>
            ))
          ) : (
            currentItems.map((booking) => (
              <div key={booking.id} className="bg-[#1F2023] rounded-xl border border-[#2e303a] px-5 py-4">
                <div className="space-y-1">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-200">{serviceName(booking.serviceId)}</span>
                    <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[booking.status] ?? 'bg-gray-500/20 text-gray-400'}`}>
                      {booking.status}
                    </span>
                  </div>
                  <p className="text-sm text-gray-400">with {formatPerson(usersMap[booking.clientId])}</p>
                  <p className="text-xs text-gray-600">
                    {formatDateTimeRange(booking.requestedStartAt, booking.requestedEndAt)}
                  </p>
                  {booking.rejectionReason && (
                    <p className="text-xs text-red-400 mt-1">Reason: {booking.rejectionReason}</p>
                  )}
                </div>
              </div>
            ))
          )}
        </div>
      )}
    </div>
  )
}
