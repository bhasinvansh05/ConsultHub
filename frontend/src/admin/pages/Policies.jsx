import { useState } from 'react'
import { updatePolicy } from '../../shared/lib/api'
import { getUserId } from '../../shared/lib/auth'

const KEY_TEMPLATES = {
  '': {},
  'PRICING_STRATEGY': { strategyType: 'DYNAMIC', dynamicMultiplier: 1.2, discountPercentage: 0.15 },
  'REFUND_POLICY': { tiers: [ { hoursBefore: 24, refundPercentage: 1.0 }, { hoursBefore: 12, refundPercentage: 0.4 }, { hoursBefore: 5, refundPercentage: 0.1 } ] },
  'NOTIFICATION_SETTINGS': { enabled: true }
}

export default function AdminPolicies() {
  const [key, setKey] = useState('')
  const [formData, setFormData] = useState({})
  const [loading, setLoading] = useState(false)
  const [success, setSuccess] = useState('')
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)
    try {
      const adminId = getUserId() ? String(getUserId()) : 'admin';
      await updatePolicy(key, { adminId, policyValue: JSON.stringify(formData) })
      setSuccess(`Policy "${key}" saved.`)
      setKey('')
      setFormData({})
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to save policy')
    } finally {
      setLoading(false)
    }
  }

  const handleFormChange = (field, val) => {
    setFormData(prev => ({ ...prev, [field]: val }))
  }

  const renderFormFields = () => {
    if (!key) return null;

    if (key === 'NOTIFICATION_SETTINGS') {
      return (
        <div className="space-y-3 p-4 bg-[#16171d] border border-[#333333] rounded-lg">
          <label className="flex items-center space-x-3 text-sm text-gray-300 cursor-pointer">
            <input 
              type="checkbox"
              checked={formData.enabled ?? true}
              onChange={(e) => handleFormChange('enabled', e.target.checked)}
              className="w-4 h-4 bg-gray-900 border-gray-700 rounded text-indigo-600 focus:ring-indigo-500"
            />
            <span>Enable System Notifications</span>
          </label>
        </div>
      )
    }

    if (key === 'PRICING_STRATEGY') {
      const isDynamic = formData.strategyType === 'DYNAMIC'
      const isDiscounted = formData.strategyType === 'DISCOUNTED'

      return (
        <div className="space-y-4 p-4 bg-[#16171d] border border-[#333333] rounded-lg">
          <div>
            <label className="block text-xs font-medium text-gray-400 mb-1">Strategy Type</label>
            <select
              value={formData.strategyType || 'FIXED'}
              onChange={(e) => handleFormChange('strategyType', e.target.value)}
              className="w-full bg-[#1F2023] border border-[#333333] rounded-md px-3 py-2 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              <option value="FIXED">FIXED</option>
              <option value="DYNAMIC">DYNAMIC</option>
              <option value="DISCOUNTED">DISCOUNTED</option>
            </select>
          </div>
          <div className="grid grid-cols-1 gap-4">
            {isDynamic && (
              <div>
                <label className="block text-xs font-medium text-gray-400 mb-1">Dynamic Multiplier</label>
                <input
                  type="number" step="0.01"
                  value={formData.dynamicMultiplier ?? 1.0}
                  onChange={(e) => handleFormChange('dynamicMultiplier', parseFloat(e.target.value))}
                  className="w-full bg-[#1F2023] border border-[#333333] rounded-md px-3 py-2 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
            )}
            {isDiscounted && (
              <div>
                <label className="block text-xs font-medium text-gray-400 mb-1">Discount % (1–100)</label>
                <input
                  type="number" step="1" min="1" max="100"
                  value={Math.round((formData.discountPercentage ?? 0) * 100) || ''}
                  onChange={(e) => handleFormChange('discountPercentage', parseFloat(e.target.value) / 100)}
                  className="w-full bg-[#1F2023] border border-[#333333] rounded-md px-3 py-2 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
            )}
          </div>
        </div>
      )
    }

    if (key === 'REFUND_POLICY') {
      const tiers = formData.tiers || [];
      return (
        <div className="space-y-4 p-4 bg-[#16171d] border border-[#333333] rounded-lg">
          <label className="block text-xs font-medium text-gray-400">Refund Tiers</label>
          {tiers.map((tier, idx) => (
            <div key={idx} className="flex items-center gap-3">
              <div className="flex-1">
                <label className="block text-[10px] text-gray-500 uppercase tracking-wider mb-1">Hours Before</label>
                <input
                  type="number"
                  value={tier.hoursBefore}
                  onChange={(e) => {
                    const newTiers = [...tiers];
                    newTiers[idx].hoursBefore = parseInt(e.target.value, 10) || 0;
                    handleFormChange('tiers', newTiers);
                  }}
                  className="w-full bg-[#1F2023] border border-[#333333] rounded-md px-3 py-1.5 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
              <div className="flex-1">
                <label className="block text-[10px] text-gray-500 uppercase tracking-wider mb-1">Refund % (1–100)</label>
                <input
                  type="number" step="1" min="1" max="100"
                  value={Math.round((tier.refundPercentage ?? 0) * 100) || ''}
                  onChange={(e) => {
                    const newTiers = [...tiers];
                    newTiers[idx].refundPercentage = (parseFloat(e.target.value) || 0) / 100;
                    handleFormChange('tiers', newTiers);
                  }}
                  className="w-full bg-[#1F2023] border border-[#333333] rounded-md px-3 py-1.5 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
                />
              </div>
              <div className="mt-5">
                <button
                  type="button"
                  onClick={() => {
                    const newTiers = tiers.filter((_, i) => i !== idx);
                    handleFormChange('tiers', newTiers);
                  }}
                  className="px-3 py-1.5 text-xs text-red-400 hover:text-red-300 bg-red-500/10 hover:bg-red-500/20 border border-red-500/20 rounded-md transition-colors"
                >
                  Remove
                </button>
              </div>
            </div>
          ))}
          <button
            type="button"
            onClick={() => handleFormChange('tiers', [...tiers, { hoursBefore: 0, refundPercentage: 0 }])}
            className="w-full mt-2 py-2 text-xs font-medium text-indigo-400 hover:text-indigo-300 bg-indigo-500/10 hover:bg-indigo-500/20 border border-indigo-500/20 rounded-md border-dashed transition-colors"
          >
            + Add Tier
          </button>
        </div>
      )
    }

    return null;
  }

  return (
    <div className="p-6 max-w-xl mx-auto">
      <h2 className="text-2xl font-semibold text-white mb-6">System Policies</h2>

      <div className="bg-[#1F2023] rounded-xl border border-[#2e303a] p-6">
        <p className="text-sm text-gray-500 mb-4">
          Create or update a system-wide policy by key. Existing keys are overwritten.
        </p>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-300 mb-1">Policy key</label>
            <select
              required
              value={key}
              onChange={(e) => {
                const newKey = e.target.value
                setKey(newKey)
                if (KEY_TEMPLATES[newKey] !== undefined) {
                  // Deep clone the template object to avoid modifying the original
                  setFormData(JSON.parse(JSON.stringify(KEY_TEMPLATES[newKey])))
                }
              }}
              className="w-full bg-[#16171d] border border-[#333333] rounded-lg px-3 py-2 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
            >
              <option value="" disabled>Select a policy key...</option>
              <option value="PRICING_STRATEGY">PRICING_STRATEGY</option>
              <option value="REFUND_POLICY">REFUND_POLICY</option>
              <option value="NOTIFICATION_SETTINGS">NOTIFICATION_SETTINGS</option>
            </select>
          </div>
          
          {renderFormFields()}

          {success && (
            <div className="text-sm text-green-400 bg-green-500/10 border border-green-500/30 rounded-lg px-3 py-2">{success}</div>
          )}
          {error && (
            <div className="text-sm text-red-400 bg-red-500/10 border border-red-500/30 rounded-lg px-3 py-2">{error}</div>
          )}

          <button
            type="submit"
            disabled={loading || !key}
            className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2.5 rounded-lg text-sm transition-colors disabled:opacity-50"
          >
            {loading ? 'Saving…' : 'Save Policy'}
          </button>
        </form>
      </div>
    </div>
  )
}
