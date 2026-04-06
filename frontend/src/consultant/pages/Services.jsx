import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getServices } from '../../shared/lib/api'

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

export default function ConsultantServices() {
  const [services, setServices] = useState([])
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()

  useEffect(() => {
    getServices()
      .then((r) => setServices(r.data))
      .catch(() => setServices([]))
      .finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="p-8 text-center text-gray-500">Loading…</div>

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <div className="mb-6">
        <h2 className="text-2xl font-semibold text-white">Platform Services</h2>
        <p className="text-sm text-gray-400 mt-1">
          Add your availability for any service below so clients can book you.
        </p>
      </div>

      {services.length === 0 ? (
        <p className="text-gray-500 text-sm">No services available yet.</p>
      ) : (
        <div className="space-y-3">
          {services.map((svc) => {
            const color = TYPE_COLORS[svc.serviceType?.toUpperCase()] ?? 'bg-gray-500/20 text-gray-400'
            return (
              <div key={svc.id} className="bg-[#1F2023] rounded-xl border border-[#2e303a] px-5 py-4 flex items-center justify-between gap-4">
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-1">
                    <p className="font-medium text-gray-100">{svc.title}</p>
                    {svc.serviceType && (
                      <span className={`text-xs px-2 py-0.5 rounded-full font-medium whitespace-nowrap ${color}`}>
                        {svc.serviceType}
                      </span>
                    )}
                  </div>
                  {svc.description && (
                    <p className="text-sm text-gray-500 truncate">{svc.description}</p>
                  )}
                  <div className="flex items-center gap-4 mt-1 text-xs text-gray-500">
                    <span>⏱ {svc.durationMinutes} min</span>
                    <span className="font-semibold text-gray-300">${Number(svc.basePrice).toFixed(2)}</span>
                  </div>
                </div>
                <button
                  onClick={() => navigate('/consultant/availability', { state: { serviceId: svc.id } })}
                  className="flex-shrink-0 text-sm bg-indigo-600 hover:bg-indigo-700 text-white px-4 py-2 rounded-lg transition-colors"
                >
                  + Add Availability
                </button>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
