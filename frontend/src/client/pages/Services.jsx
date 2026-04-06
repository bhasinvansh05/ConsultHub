import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getServices, getAvailabilityByService, getUsers } from '../../shared/lib/api'
import { isLoggedIn } from '../../shared/lib/auth'

const TYPE_COLORS = {
  CAREER: 'bg-blue-500/20 text-blue-400',
  INTERVIEW: 'bg-purple-500/20 text-purple-400',
  RESUME: 'bg-green-500/20 text-green-400',
  MENTORING: 'bg-orange-500/20 text-orange-400',
  LEGAL: 'bg-red-500/20 text-red-400',
  FINANCIAL: 'bg-emerald-500/20 text-emerald-400',
  TECH: 'bg-indigo-500/20 text-indigo-400',
  BUSINESS: 'bg-yellow-500/20 text-yellow-400',
}

function ConsultantPanel({ service, onBook }) {
  const [consultants, setConsultants] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const [slotsRes, usersRes] = await Promise.all([
          getAvailabilityByService(service.id),
          getUsers(),
        ])

        const userMap = {}
        for (const u of usersRes.data) userMap[u.id] = u

        // Group available slots by consultantId
        const byConsultant = {}
        for (const slot of slotsRes.data) {
          if (slot.isAvailable === false) continue
          if (!byConsultant[slot.consultantId]) byConsultant[slot.consultantId] = []
          byConsultant[slot.consultantId].push(slot)
        }

        const result = Object.entries(byConsultant).map(([cId, slots]) => ({
          ...(userMap[cId] ?? { id: cId }),
          slots,
        }))

        setConsultants(result)
      } catch {
        setConsultants([])
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [service.id])

  if (loading) {
    return (
      <div className="mt-4 pt-4 border-t border-[#2e303a] text-center py-6 text-gray-500 text-sm">
        Finding available consultants…
      </div>
    )
  }

  if (!consultants || consultants.length === 0) {
    return (
      <div className="mt-4 pt-4 border-t border-[#2e303a] text-center py-4 text-gray-500 text-sm">
        No consultants available for this service right now.
      </div>
    )
  }

  return (
    <div className="mt-4 pt-4 border-t border-[#2e303a] space-y-4">
      <p className="text-sm font-medium text-gray-300">
        {consultants.length} consultant{consultants.length > 1 ? 's' : ''} available
      </p>
      {consultants.map((c) => (
        <div key={c.id} className="bg-[#16171d] rounded-xl border border-[#2e303a] p-4">
          {/* Consultant info */}
          <div className="flex items-center gap-3 mb-3">
            <div className="w-9 h-9 rounded-full bg-indigo-600/30 text-indigo-400 font-semibold flex items-center justify-center text-sm flex-shrink-0">
              {(c.firstName?.[0] ?? '?')}
            </div>
            <div>
              <p className="font-medium text-gray-100 text-sm">
                {c.firstName} {c.lastName}
              </p>
              <p className="text-xs text-gray-500">{c.email}</p>
            </div>
            <div className="ml-auto text-right flex flex-col items-end">
              <p className="text-sm font-semibold text-gray-100">
                {service.originalPrice && Number(service.originalPrice) > Number(service.basePrice) ? (
                  <>
                    <span className="text-xs text-gray-500 line-through mr-2">${Number(service.originalPrice).toFixed(2)}</span>
                    <span className="text-green-400">${Number(service.basePrice).toFixed(2)}</span>
                  </>
                ) : (
                  <span>${Number(service.basePrice).toFixed(2)}</span>
                )}
              </p>
              <p className="text-xs text-gray-500 mt-1">{service.durationMinutes} min</p>
            </div>
          </div>

          {/* Available slots */}
          <p className="text-xs font-medium text-gray-500 uppercase tracking-wide mb-2">
            Available slots
          </p>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-2">
            {c.slots.slice(0, 4).map((slot) => (
              <button
                key={slot.id}
                onClick={() => onBook(slot, c, service)}
                className="flex items-center justify-between bg-[#1F2023] hover:bg-indigo-600/10 border border-[#333333] hover:border-indigo-500/50 rounded-lg px-3 py-2 transition-colors text-left group"
              >
                <div>
                  <p className="text-xs font-medium text-gray-200">
                    {new Date(slot.startAt).toLocaleDateString(undefined, {
                      weekday: 'short', month: 'short', day: 'numeric',
                    })}
                  </p>
                  <p className="text-xs text-gray-500">
                    {new Date(slot.startAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                    {' – '}
                    {new Date(slot.endAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </p>
                </div>
                <span className="text-xs font-medium text-indigo-400 group-hover:text-indigo-300 ml-2">
                  Select →
                </span>
              </button>
            ))}
          </div>
          {c.slots.length > 4 && (
            <p className="text-xs text-gray-500 mt-1">
              +{c.slots.length - 4} more slots available
            </p>
          )}
        </div>
      ))}
    </div>
  )
}

function ServiceCard({ svc, expanded, onToggle, onBook, loggedIn, onLoginPrompt }) {
  const color = TYPE_COLORS[svc.serviceType?.toUpperCase()] ?? 'bg-gray-100 text-gray-600'

  return (
    <div className={`bg-[#1F2023] rounded-xl border transition-shadow ${
      expanded ? 'border-indigo-500/50 shadow-lg shadow-indigo-500/5' : 'border-[#2e303a] hover:border-[#3a3c48]'
    }`}>
      <div className="p-5">
        <div className="flex items-start justify-between gap-2 mb-2">
          <h3 className="font-semibold text-gray-100 text-base leading-tight">{svc.title}</h3>
          {svc.serviceType && (
            <span className={`text-xs px-2 py-0.5 rounded-full font-medium whitespace-nowrap ${color}`}>
              {svc.serviceType}
            </span>
          )}
        </div>
        {svc.description && (
          <p className="text-sm text-gray-400 mb-3">{svc.description}</p>
        )}
        <div className="flex items-center gap-4 text-sm mb-4">
          <span className="text-gray-500">⏱ {svc.durationMinutes} min</span>
          {svc.originalPrice && Number(svc.originalPrice) > Number(svc.basePrice) ? (
            <div className="flex items-center gap-2">
              <span className="font-semibold text-gray-500 line-through">${Number(svc.originalPrice).toFixed(2)}</span>
              <span className="font-semibold text-green-400">${Number(svc.basePrice).toFixed(2)}</span>
            </div>
          ) : (
            <span className="font-semibold text-gray-200">${Number(svc.basePrice).toFixed(2)}</span>
          )}
        </div>
        <button
          onClick={() => loggedIn ? onToggle() : onLoginPrompt()}
          className={`w-full text-sm font-medium py-2 rounded-lg transition-colors ${
            expanded
              ? 'bg-[#2e303a] hover:bg-[#3a3c48] text-gray-300'
              : 'bg-indigo-600 hover:bg-indigo-700 text-white'
          }`}
        >
          {expanded ? 'Close' : 'Book this service'}
        </button>
      </div>

      {expanded && (
        <div className="px-5 pb-5">
          <ConsultantPanel service={svc} onBook={onBook} />
        </div>
      )}
    </div>
  )
}

export default function ClientServices() {
  const [services, setServices] = useState([])
  const [loading, setLoading] = useState(true)
  const [expandedId, setExpandedId] = useState(null)
  const [showLoginPrompt, setShowLoginPrompt] = useState(false)
  const navigate = useNavigate()
  const loggedIn = isLoggedIn()

  useEffect(() => {
    getServices()
      .then((r) => setServices(r.data))
      .catch(() => setServices([]))
      .finally(() => setLoading(false))
  }, [])

  const handleBook = (slot, consultant, service) => {
    navigate('/client/book', { state: { slot, consultant, service } })
  }

  const handleToggle = (id) => {
    setExpandedId((prev) => (prev === id ? null : id))
    setShowLoginPrompt(false)
  }

  return (
    <div className="max-w-5xl mx-auto p-6">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-white">Consulting Services</h1>
        <p className="text-gray-400 mt-1">
          Browse our services and book a session with an available consultant.
        </p>
      </div>

      {loading ? (
        <div className="text-center py-16 text-gray-500">Loading services…</div>
      ) : services.length === 0 ? (
        <div className="text-center py-16 text-gray-500">
          <p className="text-lg">No services available yet.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {services.map((svc) => (
            <ServiceCard
              key={svc.id}
              svc={svc}
              expanded={expandedId === svc.id}
              onToggle={() => handleToggle(svc.id)}
              onBook={handleBook}
              loggedIn={loggedIn}
              onLoginPrompt={() => setShowLoginPrompt(true)}
            />
          ))}
        </div>
      )}

      {showLoginPrompt && (
        <div className="mt-8 bg-indigo-600/10 border border-indigo-500/30 rounded-xl py-6 px-4 text-center">
          <p className="text-gray-200 font-medium">Sign in to book a session</p>
          <p className="text-sm text-gray-400 mt-1 mb-4">
            Create a free client account to get started.
          </p>
          <div className="flex justify-center gap-3">
            <button
              onClick={() => navigate('/login')}
              className="bg-indigo-600 hover:bg-indigo-700 text-white text-sm font-medium px-5 py-2 rounded-lg transition-colors"
            >
              Sign in
            </button>
            <button
              onClick={() => navigate('/register')}
              className="bg-[#1F2023] hover:bg-[#2e303a] text-indigo-400 border border-indigo-500/40 text-sm font-medium px-5 py-2 rounded-lg transition-colors"
            >
              Register
            </button>
          </div>
        </div>
      )}
    </div>
  )
}
