import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { processPayment, getPaymentHistory, getPaymentMethods, addPaymentMethod, deletePaymentMethod, getClientBookings, getServices, getUsers } from '../../shared/lib/api'
import { getUserId } from '../../shared/lib/auth'
import { PaymentMethodSelector } from '../../shared/components/PaymentMethodSelector'
import { CheckCircle } from 'lucide-react'

const PAYMENT_TYPES = ['CREDIT_CARD', 'DEBIT_CARD', 'PAYPAL', 'BANK_TRANSFER']

const STATUS_COLORS = {
  SUCCESS: 'bg-green-500/20 text-green-400',
  FAILED: 'bg-red-500/20 text-red-400',
  PENDING: 'bg-yellow-500/20 text-yellow-400',
  REFUNDED: 'bg-gray-500/20 text-gray-400',
}

const TYPE_ICONS = {
  CREDIT_CARD: (
    <svg className="w-8 h-6" viewBox="0 0 32 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="32" height="24" rx="4" fill="#1e40af" />
      <rect x="0" y="8" width="32" height="5" fill="#1d4ed8" />
      <rect x="4" y="15" width="8" height="3" rx="1" fill="#93c5fd" />
    </svg>
  ),
  DEBIT_CARD: (
    <svg className="w-8 h-6" viewBox="0 0 32 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="32" height="24" rx="4" fill="#065f46" />
      <rect x="0" y="8" width="32" height="5" fill="#047857" />
      <rect x="4" y="15" width="8" height="3" rx="1" fill="#6ee7b7" />
    </svg>
  ),
  PAYPAL: (
    <svg className="w-8 h-6" viewBox="0 0 32 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="32" height="24" rx="4" fill="#1e3a5f" />
      <text x="4" y="16" fontSize="9" fill="#60a5fa" fontFamily="Arial" fontWeight="bold">PayPal</text>
    </svg>
  ),
  BANK_TRANSFER: (
    <svg className="w-8 h-6" viewBox="0 0 32 24" fill="none" xmlns="http://www.w3.org/2000/svg">
      <rect width="32" height="24" rx="4" fill="#3b1f6e" />
      <text x="4" y="16" fontSize="8" fill="#c4b5fd" fontFamily="Arial">BANK</text>
    </svg>
  ),
}

function Field({ label, value, onChange, placeholder, type = 'text', required = true }) {
  return (
    <div>
      <label className="block text-sm font-medium text-gray-300 mb-1">{label}</label>
      <input
        type={type}
        required={required}
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        className="w-full bg-[#16171d] border border-[#333333] rounded-lg px-3 py-2 text-sm text-gray-100 placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
      />
    </div>
  )
}

function PaymentFields({ type, fields, set, lockedFields = [] }) {
  const isLocked = (k) => lockedFields.includes(k)
  const fieldClass = (k) =>
    `w-full bg-[#16171d] border rounded-lg px-3 py-2 text-sm placeholder-gray-600 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent ${
      isLocked(k)
        ? 'border-[#2e303a] text-gray-500 cursor-not-allowed'
        : 'border-[#333333] text-gray-100'
    }`

  if (type === 'CREDIT_CARD' || type === 'DEBIT_CARD') return (
    <>
      <div>
        <label className="block text-sm font-medium text-gray-300 mb-1">Card number</label>
        <input
          type="text"
          value={fields.cardNumber ?? ''}
          onChange={(e) => !isLocked('cardNumber') && set('cardNumber', e.target.value)}
          placeholder="4111 1111 1111 1111"
          readOnly={isLocked('cardNumber')}
          className={fieldClass('cardNumber')}
        />
      </div>
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-1">Expiry (MM/YY)</label>
          <input
            type="text"
            value={fields.expiryDate ?? ''}
            onChange={(e) => !isLocked('expiryDate') && set('expiryDate', e.target.value)}
            placeholder="12/26"
            readOnly={isLocked('expiryDate')}
            className={fieldClass('expiryDate')}
          />
        </div>
        <Field label="CVV" value={fields.cvv ?? ''} onChange={(v) => set('cvv', v)} placeholder="123" />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-300 mb-1">Cardholder name</label>
        <input
          type="text"
          value={fields.cardholderName ?? ''}
          onChange={(e) => !isLocked('cardholderName') && set('cardholderName', e.target.value)}
          placeholder="John Doe"
          readOnly={isLocked('cardholderName')}
          className={fieldClass('cardholderName')}
        />
      </div>
    </>
  )
  if (type === 'PAYPAL') return (
    <div>
      <label className="block text-sm font-medium text-gray-300 mb-1">PayPal email</label>
      <input
        type="email"
        value={fields.paypalEmail ?? ''}
        onChange={(e) => !isLocked('paypalEmail') && set('paypalEmail', e.target.value)}
        placeholder="you@paypal.com"
        readOnly={isLocked('paypalEmail')}
        className={fieldClass('paypalEmail')}
      />
    </div>
  )
  if (type === 'BANK_TRANSFER') return (
    <>
      <div>
        <label className="block text-sm font-medium text-gray-300 mb-1">Account number</label>
        <input
          type="text"
          value={fields.accountNumber ?? ''}
          onChange={(e) => !isLocked('accountNumber') && set('accountNumber', e.target.value)}
          placeholder="123456789"
          readOnly={isLocked('accountNumber')}
          className={fieldClass('accountNumber')}
        />
      </div>
      <div>
        <label className="block text-sm font-medium text-gray-300 mb-1">Routing number</label>
        <input
          type="text"
          value={fields.routingNumber ?? ''}
          onChange={(e) => !isLocked('routingNumber') && set('routingNumber', e.target.value)}
          placeholder="021000021"
          readOnly={isLocked('routingNumber')}
          className={fieldClass('routingNumber')}
        />
      </div>
    </>
  )
  return null
}

function autofillFromSaved(method) {
  const payType = method.paymentType ?? method.type
  if (payType === 'CREDIT_CARD' || payType === 'DEBIT_CARD') {
    return {
      cardNumber: method.last4Digits ? `**** **** **** ${method.last4Digits}` : '',
      expiryDate: method.expiryDate ?? '',
      cardholderName: method.cardholderName ?? '',
      cvv: '',
    }
  }
  if (payType === 'PAYPAL') return { paypalEmail: method.paypalEmail ?? '' }
  if (payType === 'BANK_TRANSFER') return {
    accountNumber: method.last4AccountDigits ? `****${method.last4AccountDigits}` : '',
    routingNumber: method.routingNumber ?? '',
  }
  return {}
}

const CARD_LOCKED_FIELDS = ['cardNumber', 'expiryDate', 'cardholderName']
const PAYPAL_LOCKED_FIELDS = ['paypalEmail']
const BANK_LOCKED_FIELDS = ['accountNumber', 'routingNumber']

function lockedFieldsFor(payType) {
  if (payType === 'CREDIT_CARD' || payType === 'DEBIT_CARD') return CARD_LOCKED_FIELDS
  if (payType === 'PAYPAL') return PAYPAL_LOCKED_FIELDS
  if (payType === 'BANK_TRANSFER') return BANK_LOCKED_FIELDS
  return []
}

function getBookingLabel(booking, fallback = 'this session') {
  return booking?.serviceName ?? booking?.serviceTitle ?? fallback
}

function PayNowForm({ booking, savedMethods, clientId, onSuccess }) {
  const [selectedSavedId, setSelectedSavedId] = useState(null)
  const [type, setType] = useState('CREDIT_CARD')
  const [fields, setFields] = useState({})
  const [saveMethod, setSaveMethod] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const set = (k, v) => setFields((f) => ({ ...f, [k]: v }))

  const handleSelectSaved = (id) => {
    setSelectedSavedId(id)
    if (id === null) { setFields({}); return }
    const m = savedMethods.find((m) => m.id === id)
    if (!m) return
    setType(m.paymentType ?? m.type ?? 'CREDIT_CARD')
    setFields(autofillFromSaved(m))
  }

  const activeSaved = selectedSavedId != null
    ? savedMethods.find((m) => m.id === selectedSavedId)
    : null

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      let paymentDetails
      let savedPaymentMethodId = null
      if (activeSaved) {
        savedPaymentMethodId = activeSaved.id
        paymentDetails = {
          type: activeSaved.paymentType ?? activeSaved.type ?? type,
          cvv: fields.cvv ?? '',
        }
      } else {
        const sanitized = {
          type,
          ...fields,
          ...(fields.cardNumber ? { cardNumber: fields.cardNumber.replace(/\s+/g, '') } : {}),
          ...(fields.cvv ? { cvv: fields.cvv.replace(/\s+/g, '') } : {}),
        }
        if (saveMethod) {
          await addPaymentMethod(clientId, sanitized)
        }
        paymentDetails = sanitized
      }
      await processPayment({
        bookingId: booking.id,
        clientId: Number(clientId),
        amount: booking.amount,
        savedPaymentMethodId,
        paymentDetails,
      })
      onSuccess()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Payment failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* Saved method selector */}
      {savedMethods.length > 0 && (
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-2">Payment method</label>
          <div className="space-y-2">
            {savedMethods.map((m) => {
              const payType = m.paymentType ?? m.type ?? ''
              const desc = m.last4Digits ? `•••• ${m.last4Digits}` : m.paypalEmail ?? ''
              const isSelected = selectedSavedId === m.id
              return (
                <div
                  key={m.id}
                  onClick={() => handleSelectSaved(isSelected ? null : m.id)}
                  className={`flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-all ${
                    isSelected
                      ? 'border-indigo-500 bg-indigo-500/10'
                      : 'border-[#2e303a] bg-[#16171d] hover:border-[#444]'
                  }`}
                >
                  <div className="flex-shrink-0">{TYPE_ICONS[payType] ?? TYPE_ICONS.CREDIT_CARD}</div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-200">{payType.replace(/_/g, ' ')}</p>
                    {desc && <p className="text-xs text-gray-500">{desc}</p>}
                  </div>
                  <div className={`w-4 h-4 rounded-full border-2 flex-shrink-0 flex items-center justify-center ${isSelected ? 'border-indigo-500' : 'border-[#444]'}`}>
                    {isSelected && <div className="w-2 h-2 rounded-full bg-indigo-500" />}
                  </div>
                </div>
              )
            })}
            <div
              onClick={() => handleSelectSaved(null)}
              className={`flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-all ${
                selectedSavedId === null
                  ? 'border-indigo-500 bg-indigo-500/10'
                  : 'border-[#2e303a] bg-[#16171d] hover:border-[#444]'
              }`}
            >
              <div className="flex-1 text-sm font-medium text-gray-200">Use a new payment method</div>
              <div className={`w-4 h-4 rounded-full border-2 flex-shrink-0 flex items-center justify-center ${selectedSavedId === null ? 'border-indigo-500' : 'border-[#444]'}`}>
                {selectedSavedId === null && <div className="w-2 h-2 rounded-full bg-indigo-500" />}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Type selector only shown for new method or if no saved methods */}
      {(selectedSavedId === null) && (
        <div>
          <label className="block text-sm font-medium text-gray-300 mb-1">
            {savedMethods.length > 0 ? 'New payment type' : 'Payment method'}
          </label>
          <select value={type} onChange={(e) => { setType(e.target.value); setFields({}) }}
            className="w-full bg-[#16171d] border border-[#333333] rounded-lg px-3 py-2 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500">
            {PAYMENT_TYPES.map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
          </select>
        </div>
      )}

      {/* Payment fields — autofilled when saved method selected */}
      {activeSaved && (type === 'CREDIT_CARD' || type === 'DEBIT_CARD') && (
        <p className="text-xs text-indigo-400 bg-indigo-500/10 border border-indigo-500/20 rounded-lg px-3 py-2">
          Card details autofilled — enter your CVV to proceed.
        </p>
      )}
      <PaymentFields
        type={type}
        fields={fields}
        set={set}
        lockedFields={activeSaved ? lockedFieldsFor(type) : []}
      />

      {/* Save method checkbox — only for new entries */}
      {selectedSavedId === null && (
        <label className="flex items-center gap-2.5 cursor-pointer select-none">
          <div
            onClick={() => setSaveMethod((v) => !v)}
            className={`w-5 h-5 rounded border-2 flex items-center justify-center flex-shrink-0 transition-colors ${
              saveMethod ? 'bg-indigo-600 border-indigo-600' : 'border-[#444] bg-[#16171d]'
            }`}
          >
            {saveMethod && <CheckCircle className="w-3 h-3 text-white" />}
          </div>
          <span className="text-sm text-gray-400">Save this payment method for future use</span>
        </label>
      )}

      {error && <div className="text-sm text-red-400 bg-red-500/10 border border-red-500/30 rounded-lg px-3 py-2">{error}</div>}
      <button type="submit" disabled={loading}
        className="w-full bg-green-600 hover:bg-green-700 text-white font-medium py-2.5 rounded-lg text-sm transition-colors disabled:opacity-50">
        {loading ? 'Processing…' : `Pay $${booking.amount} for ${getBookingLabel(booking)}`}
      </button>
    </form>
  )
}

function AddMethodForm({ clientId, onAdded }) {
  const [type, setType] = useState('CREDIT_CARD')
  const [fields, setFields] = useState({})
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const set = (k, v) => setFields((f) => ({ ...f, [k]: v }))

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const sanitized = {
        ...fields,
        ...(fields.cardNumber ? { cardNumber: fields.cardNumber.replace(/\s+/g, '') } : {}),
        ...(fields.cvv ? { cvv: fields.cvv.replace(/\s+/g, '') } : {}),
      }
      await addPaymentMethod(clientId, { type, ...sanitized })
      setFields({})
      onAdded()
    } catch (err) {
      setError(err.response?.data?.message ?? 'Failed to save method')
    } finally {
      setLoading(false)
    }
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="block text-sm font-medium text-gray-300 mb-1">Payment type</label>
        <select value={type} onChange={(e) => { setType(e.target.value); setFields({}) }}
          className="w-full bg-[#16171d] border border-[#333333] rounded-lg px-3 py-2 text-sm text-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500">
          {PAYMENT_TYPES.map((t) => <option key={t} value={t}>{t.replace(/_/g, ' ')}</option>)}
        </select>
      </div>
      <PaymentFields type={type} fields={fields} set={set} />
      {error && <div className="text-sm text-red-400 bg-red-500/10 border border-red-500/30 rounded-lg px-3 py-2">{error}</div>}
      <button type="submit" disabled={loading}
        className="w-full bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-2 rounded-lg text-sm transition-colors disabled:opacity-50">
        {loading ? 'Saving…' : 'Save Payment Method'}
      </button>
    </form>
  )
}

export default function ClientPayments() {
  const { state } = useLocation()
  const clientId = getUserId()
  const [history, setHistory] = useState([])
  const [methods, setMethods] = useState([])
  const [bookingsMap, setBookingsMap] = useState({})
  const [servicesMap, setServicesMap] = useState({})
  const [usersMap, setUsersMap] = useState({})
  const [paid, setPaid] = useState(false)
  const [showAddMethod, setShowAddMethod] = useState(false)

  const loadAll = async () => {
    try {
      const [histRes, methodsRes, bookingsRes, servicesRes, usersRes] = await Promise.all([
        getPaymentHistory(clientId).catch(() => ({ data: [] })),
        getPaymentMethods(clientId).catch(() => ({ data: [] })),
        getClientBookings(clientId).catch(() => ({ data: [] })),
        getServices().catch(() => ({ data: [] })),
        getUsers().catch(() => ({ data: [] })),
      ])
      setHistory(histRes.data)
      setMethods(methodsRes.data)
      const bMap = {}
      for (const b of bookingsRes.data) bMap[b.id] = b
      setBookingsMap(bMap)
      const sMap = {}
      for (const s of servicesRes.data) sMap[s.id] = s
      setServicesMap(sMap)
      const uMap = {}
      for (const u of usersRes.data) uMap[u.id] = u
      setUsersMap(uMap)
    } catch {}
  }

  useEffect(() => { loadAll() }, [paid])

  const handleDelete = async (methodId) => {
    if (!confirm('Remove this payment method?')) return
    try {
      await deletePaymentMethod(clientId, methodId)
      loadAll()
    } catch { /* ignore */ }
  }

  const selectorMethods = methods.map((m) => ({
    id: m.id,
    icon: TYPE_ICONS[m.paymentType ?? m.type] ?? TYPE_ICONS.CREDIT_CARD,
    label: (m.paymentType ?? m.type ?? '').replace(/_/g, ' '),
    description: m.last4Digits ? `•••• ${m.last4Digits}` : m.paypalEmail ?? '',
  }))

  return (
    <div className="p-6 max-w-2xl mx-auto space-y-8">

      {/* Pay Now (if navigated from bookings) */}
      {state?.booking && !paid && (
        <div className="bg-[#1F2023] rounded-2xl border border-[#2e303a] p-6">
          <h2 className="text-xl font-semibold text-white mb-4">
            {state.booking.serviceName ?? state.booking.serviceTitle ?? 'Complete Payment'}
          </h2>
          <PayNowForm booking={state.booking} savedMethods={methods} clientId={clientId} onSuccess={() => { setPaid(true); loadAll() }} />
        </div>
      )}
      {paid && (
        <div className="bg-green-500/10 border border-green-500/30 rounded-xl px-5 py-4 text-green-400 text-sm font-medium">
          Payment successful!
        </div>
      )}

      {/* Saved Payment Methods — hidden while a payment is in progress */}
      {!(state?.booking && !paid) && <div>
        <PaymentMethodSelector
          title="Payment Methods"
          actionText={showAddMethod ? '' : 'Add Method'}
          methods={selectorMethods}
          defaultSelectedId={selectorMethods[0]?.id}
          onActionClick={() => setShowAddMethod((v) => !v)}
          onDelete={handleDelete}
        />

        {methods.length === 0 && !showAddMethod && (
          <p className="text-sm text-gray-500 mt-3 px-1">No saved payment methods.</p>
        )}

        {showAddMethod && (
          <div className="mt-4 bg-[#1F2023] rounded-xl border border-[#2e303a] p-6">
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold text-gray-200">Add New Method</h3>
              <button onClick={() => setShowAddMethod(false)} className="text-xs text-gray-500 hover:text-gray-300">✕ Cancel</button>
            </div>
            <AddMethodForm clientId={clientId} onAdded={() => { setShowAddMethod(false); loadAll() }} />
          </div>
        )}
      </div>}

      {/* Payment History */}
      <div>
        <h2 className="text-xl font-semibold text-white mb-4">Payment History</h2>
        {history.length === 0 ? (
          <p className="text-gray-500 text-sm">No payments yet.</p>
        ) : (
          <div className="space-y-3">
            {history.flatMap((p) => {
              const booking = bookingsMap[p.bookingId]
              const service = booking ? servicesMap[booking.serviceId] : null
              const consultant = booking ? usersMap[booking.consultantId] : null
              const consultantName = consultant
                ? (consultant.firstName && consultant.lastName ? `${consultant.firstName} ${consultant.lastName}` : consultant.email)
                : null
              const date = p.timestamp ? new Date(p.timestamp) : null
              const validDate = date && !isNaN(date)

              const rows = []
              
              // Original payment row
              const displayStatus = p.status === 'REFUNDED' ? 'SUCCESS' : p.status

              rows.push(
                <div key={p.id} className="bg-[#1F2023] rounded-xl border border-[#2e303a] px-5 py-4">
                  <div className="flex items-start justify-between gap-4">
                    <div className="space-y-1 min-w-0">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="font-medium text-gray-100">
                          {service?.title ?? (consultantName ? `Session with ${consultantName}` : 'Consulting Session')}
                        </span>
                        <span className={`text-xs px-2 py-0.5 rounded-full font-medium shrink-0 ${STATUS_COLORS[displayStatus] ?? 'bg-gray-500/20 text-gray-400'}`}>
                          {displayStatus}
                        </span>
                      </div>
                      {consultantName && (
                        <p className="text-sm text-gray-400">with {consultantName}</p>
                      )}
                      <div className="flex items-center gap-3 text-xs text-gray-500">
                        {validDate && <span>{date.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}</span>}
                        {p.paymentType && <span className="text-gray-600">· {p.paymentType.replace(/_/g, ' ')}</span>}
                        <span className="text-gray-600">· TXN {p.transactionId?.slice(-8)}</span>
                      </div>
                      {p.failureReason && (
                        <p className="text-xs text-red-400">{p.failureReason}</p>
                      )}
                    </div>
                    <span className="text-lg font-semibold text-gray-100 shrink-0">
                      ${p.amount}
                    </span>
                  </div>
                </div>
              )

              // Refund row if applicable
              if (p.status === 'REFUNDED' && p.refundAmount != null) {
                const refundDate = p.refundedAt ? new Date(p.refundedAt) : null
                const validRefundDate = refundDate && !isNaN(refundDate)

                rows.push(
                  <div key={`${p.id}-refund`} className="bg-[#1F2023] rounded-xl border border-[#2e303a] px-5 py-4 relative overflow-hidden">
                    <div className="absolute left-0 top-0 bottom-0 w-1 bg-green-500"></div>
                    <div className="flex items-start justify-between gap-4">
                      <div className="space-y-1 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className="font-medium text-gray-100">
                            Refund for {service?.title ?? (consultantName ? `Session with ${consultantName}` : 'Consulting Session')}
                          </span>
                          <span className="text-xs px-2 py-0.5 rounded-full font-medium shrink-0 bg-green-500/20 text-green-400">
                            REFUND
                          </span>
                        </div>
                        {consultantName && (
                          <p className="text-sm text-gray-400">with {consultantName}</p>
                        )}
                        <div className="flex items-center gap-3 text-xs text-gray-500">
                          {validRefundDate && <span>{refundDate.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' })}</span>}
                          {p.paymentType && <span className="text-gray-600">· {p.paymentType.replace(/_/g, ' ')}</span>}
                          <span className="text-gray-600">· TXN {p.transactionId?.slice(-8)}</span>
                        </div>
                      </div>
                      <span className="text-lg font-semibold text-green-400 shrink-0">
                        +${p.refundAmount}
                      </span>
                    </div>
                  </div>
                )
              }

              return rows
            })}
          </div>
        )}
      </div>
    </div>
  )
}




