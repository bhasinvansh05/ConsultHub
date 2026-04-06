import { useEffect, useState } from 'react'
import { getPendingConsultants, approveConsultant, getUsers } from '../../shared/lib/api'
import { getUserId } from '../../shared/lib/auth'

export default function AdminApprovals() {
  const [pending, setPending] = useState([])
  const [usersMap, setUsersMap] = useState({})
  const [loading, setLoading] = useState(true)
  const [reason, setReason] = useState({})

  const load = () => {
    setLoading(true)
    Promise.all([getPendingConsultants(), getUsers()])
      .then(([pendingRes, usersRes]) => {
        setPending(pendingRes.data)
        const map = {}
        for (const u of usersRes.data) map[u.id] = u
        setUsersMap(map)
      })
      .finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const consultantName = (id) => {
    const u = usersMap[id]
    if (!u) return `Consultant #${id}`
    return u.firstName && u.lastName ? `${u.firstName} ${u.lastName}` : u.email ?? `Consultant #${id}`
  }

  const decide = async (consultantId, decision) => {
    const adminId = getUserId()
    try {
      await approveConsultant(consultantId, {
        decision,
        reason: reason[consultantId] ?? '',
        adminId: String(adminId),
      })
      load()
    } catch (err) {
      alert(err.response?.data?.message ?? 'Action failed')
    }
  }

  if (loading) return <div className="p-8 text-center text-gray-500">Loading…</div>

  return (
    <div className="p-6 max-w-3xl mx-auto">
      <h2 className="text-2xl font-semibold text-white mb-6">Pending Consultant Approvals</h2>

      {pending.length === 0 ? (
        <p className="text-gray-500 text-sm">No pending registrations.</p>
      ) : (
        <div className="space-y-4">
          {pending.map((reg) => (
            <div key={reg.id} className="bg-[#1F2023] rounded-xl border border-[#2e303a] p-5">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <p className="font-medium text-gray-200">{consultantName(reg.consultantId)}</p>
                  <p className="text-xs text-gray-500 mt-0.5">
                    {usersMap[reg.consultantId]?.email && (
                      <span className="mr-2">{usersMap[reg.consultantId].email}</span>
                    )}
                    Registered {reg.createdAt ? new Date(reg.createdAt).toLocaleString() : '—'}
                  </p>
                </div>
                <span className="text-xs px-2 py-0.5 bg-yellow-500/20 text-yellow-400 rounded-full font-medium">
                  PENDING
                </span>
              </div>

              <div className="mb-3">
                <label className="block text-sm font-medium text-gray-300 mb-1">Reason (optional)</label>
                <input
                  type="text"
                  value={reason[reg.consultantId] ?? ''}
                  onChange={(e) => setReason((r) => ({ ...r, [reg.consultantId]: e.target.value }))}
                  placeholder="Add a note…"
                  className="w-full bg-[#16171d] border border-[#333333] rounded-lg px-3 py-2 text-sm text-gray-100 placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
                />
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => decide(reg.consultantId, 'APPROVE')}
                  className="flex-1 bg-green-600 hover:bg-green-700 text-white text-sm font-medium py-2 rounded-lg transition-colors"
                >
                  Approve
                </button>
                <button
                  onClick={() => decide(reg.consultantId, 'REJECT')}
                  className="flex-1 bg-red-500/10 hover:bg-red-500/20 text-red-400 text-sm font-medium py-2 rounded-lg transition-colors"
                >
                  Reject
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
