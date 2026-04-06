import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { getConsultantBookings, getServices, getUsers } from '../../shared/lib/api'
import { getUserId } from '../../shared/lib/auth'

const STATUS_COLORS = {
  REQUESTED: 'bg-yellow-500/20 text-yellow-400',
  CONFIRMED: 'bg-blue-500/20 text-blue-400',
  PAID: 'bg-green-500/20 text-green-400',
  COMPLETED: 'bg-gray-500/20 text-gray-400',
  REJECTED: 'bg-red-500/20 text-red-400',
  CANCELLED: 'bg-red-500/10 text-red-400',
}

const WEEKDAY_LABELS = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat']

function startOfDay(date) {
  const value = new Date(date)
  value.setHours(0, 0, 0, 0)
  return value
}

function formatMonthLabel(date) {
  return date.toLocaleDateString(undefined, { month: 'long', year: 'numeric' })
}

function formatPerson(user) {
  if (!user) return 'Client'
  const fullName = `${user.firstName ?? ''} ${user.lastName ?? ''}`.trim()
  return fullName || user.email || 'Client'
}

function formatTimeRange(startAt, endAt) {
  const start = new Date(startAt)
  const end = new Date(endAt)
  return `${start.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })} - ${end.toLocaleTimeString([], { hour: 'numeric', minute: '2-digit' })}`
}

export default function ConsultantDashboard() {
  const consultantId = getUserId()
  const [loading, setLoading] = useState(true)
  const [bookings, setBookings] = useState([])
  const [usersMap, setUsersMap] = useState({})
  const [servicesMap, setServicesMap] = useState({})
  const [calendarMonth, setCalendarMonth] = useState(() => {
    const today = new Date()
    return new Date(today.getFullYear(), today.getMonth(), 1)
  })

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const [bookingsRes, usersRes, servicesRes] = await Promise.all([
          getConsultantBookings(consultantId),
          getUsers(),
          getServices(),
        ])

        setBookings(bookingsRes.data ?? [])

        const nextUsersMap = {}
        for (const user of usersRes.data ?? []) nextUsersMap[user.id] = user
        setUsersMap(nextUsersMap)

        const nextServicesMap = {}
        for (const service of servicesRes.data ?? []) nextServicesMap[service.id] = service
        setServicesMap(nextServicesMap)
      } catch {
        setBookings([])
        setUsersMap({})
        setServicesMap({})
      } finally {
        setLoading(false)
      }
    }

    load()
  }, [consultantId])

  const visibleBookings = bookings
    .filter((booking) => ['REQUESTED', 'CONFIRMED', 'PAID'].includes(booking.status))
    .sort((a, b) => new Date(a.requestedStartAt) - new Date(b.requestedStartAt))

  const bookingsByDay = useMemo(() => {
    const grouped = {}
    for (const booking of visibleBookings) {
      const key = startOfDay(new Date(booking.requestedStartAt)).toISOString()
      if (!grouped[key]) grouped[key] = []
      grouped[key].push(booking)
    }
    return grouped
  }, [visibleBookings])

  const calendarDays = useMemo(() => {
    const today = startOfDay(new Date())
    const firstVisible = new Date(calendarMonth)
    firstVisible.setDate(1 - firstVisible.getDay())

    return Array.from({ length: 35 }, (_, index) => {
      const date = new Date(firstVisible)
      date.setDate(firstVisible.getDate() + index)
      const key = startOfDay(date).toISOString()

      return {
        key,
        date,
        bookings: bookingsByDay[key] ?? [],
        isCurrentMonth: date.getMonth() === calendarMonth.getMonth(),
        isToday: startOfDay(date).getTime() === today.getTime(),
      }
    })
  }, [calendarMonth, bookingsByDay])

  return (
    <div className="p-6 max-w-6xl mx-auto">
      <div className="flex items-start justify-between gap-4 mb-6">
        <div>
          <h2 className="text-2xl font-semibold text-white">Your Month at a Glance</h2>
          <p className="text-sm text-gray-400 mt-1">A simple calendar of your current consultant schedule.</p>
        </div>

        <div className="flex gap-2">
          <Link
            to="/consultant/availability"
            className="text-sm bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg transition-colors"
          >
            Add Availability
          </Link>
          <Link
            to="/consultant/bookings"
            className="text-sm bg-[#2e303a] hover:bg-[#3a3c48] text-gray-300 hover:text-white px-4 py-2 rounded-lg transition-colors"
          >
            Manage Bookings
          </Link>
        </div>
      </div>

      <div className="bg-[#1F2023] rounded-xl border border-[#2e303a] p-5">
        <div className="flex items-center justify-between mb-5">
          <button
            type="button"
            onClick={() => setCalendarMonth((value) => new Date(value.getFullYear(), value.getMonth() - 1, 1))}
            className="p-2 rounded-lg bg-[#2e303a] text-gray-300 hover:bg-[#3a3c48] hover:text-white transition-colors"
          >
            <ChevronLeft className="w-4 h-4" />
          </button>

          <div className="text-sm font-medium text-gray-200">
            {formatMonthLabel(calendarMonth)}
          </div>

          <button
            type="button"
            onClick={() => setCalendarMonth((value) => new Date(value.getFullYear(), value.getMonth() + 1, 1))}
            className="p-2 rounded-lg bg-[#2e303a] text-gray-300 hover:bg-[#3a3c48] hover:text-white transition-colors"
          >
            <ChevronRight className="w-4 h-4" />
          </button>
        </div>

        {loading ? (
          <div className="text-center text-gray-500 py-12">Loading...</div>
        ) : (
          <>
            <div className="grid grid-cols-7 gap-2 mb-2">
              {WEEKDAY_LABELS.map((label) => (
                <div key={label} className="text-center text-xs text-gray-500 py-2">
                  {label}
                </div>
              ))}
            </div>

            <div className="grid grid-cols-7 gap-2">
              {calendarDays.map((day) => {
                const hasBookings = day.bookings.length > 0

                return (
                  <div
                    key={day.key}
                    className={`min-h-[160px] rounded-xl border p-3 ${
                      hasBookings
                        ? 'bg-indigo-500/10 border-indigo-500/30'
                        : 'bg-[#16171d] border-[#2e303a]'
                    } ${!day.isCurrentMonth ? 'opacity-45' : ''}`}
                  >
                    <div className="flex items-center justify-between mb-3">
                      <span className={`text-sm font-medium ${day.isToday ? 'text-indigo-300' : 'text-gray-200'}`}>
                        {day.date.getDate()}
                      </span>
                      {hasBookings && (
                        <span className="text-[10px] px-2 py-0.5 rounded-full bg-indigo-500/20 text-indigo-300">
                          {day.bookings.length}
                        </span>
                      )}
                    </div>

                    <div className="space-y-2">
                      {hasBookings ? (
                        day.bookings.slice(0, 3).map((booking) => (
                          <div key={booking.id} className="rounded-lg bg-[#1b1c22] border border-[#2e303a] px-2 py-2">
                            <div className="flex items-center gap-2 mb-1">
                              <span className={`text-[10px] px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[booking.status] ?? 'bg-gray-500/20 text-gray-400'}`}>
                                {booking.status}
                              </span>
                            </div>
                            <p className="text-[11px] font-medium text-gray-200 truncate">
                              {servicesMap[booking.serviceId]?.title ?? 'Consulting Session'}
                            </p>
                            <p className="text-[10px] text-gray-500 truncate">
                              {formatPerson(usersMap[booking.clientId])}
                            </p>
                            <p className="text-[10px] text-gray-400 mt-1">
                              {formatTimeRange(booking.requestedStartAt, booking.requestedEndAt)}
                            </p>
                          </div>
                        ))
                      ) : (
                        <p className="text-xs text-gray-600 pt-8">No bookings</p>
                      )}

                      {day.bookings.length > 3 && (
                        <p className="text-[10px] text-indigo-300">
                          +{day.bookings.length - 3} more
                        </p>
                      )}
                    </div>
                  </div>
                )
              })}
            </div>
          </>
        )}
      </div>
    </div>
  )
}
