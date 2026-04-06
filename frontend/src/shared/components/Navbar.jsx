import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../auth/context/AuthContext'
import { useEffect, useRef, useState, useCallback } from 'react'
import { AnimatePresence, motion } from 'framer-motion'
import { Bell, X } from 'lucide-react'
import { getNotifications, getUnreadCount, markNotificationRead, markAllNotificationsRead } from '../lib/api'

const TYPE_LABEL = {
  PAYMENT_SUCCESS: 'Payment',
  CONSULTANT_PENDING_APPROVAL: 'Approval',
  BOOKING_REJECTED: 'Booking',
  BOOKING_CANCELLED: 'Booking',
  BOOKING_CONFIRMED: 'Booking',
  BOOKING_REQUESTED: 'Booking',
  PAYMENT_FAILED: 'Payment',
  PAYMENT_REFUNDED: 'Refund',
  POLICY_UPDATED: 'Policy',
}

const TYPE_COLOR = {
  PAYMENT_SUCCESS: 'text-green-400',
  CONSULTANT_PENDING_APPROVAL: 'text-amber-400',
  BOOKING_REJECTED: 'text-red-400',
  BOOKING_CANCELLED: 'text-red-400',
  BOOKING_CONFIRMED: 'text-blue-400',
  BOOKING_REQUESTED: 'text-yellow-400',
  PAYMENT_FAILED: 'text-red-400',
  PAYMENT_REFUNDED: 'text-purple-400',
  POLICY_UPDATED: 'text-gray-400',
}

function timeAgo(dateStr) {
  const diff = Math.floor((Date.now() - new Date(dateStr)) / 1000)
  if (diff < 60) return 'just now'
  if (diff < 3600) return `${Math.floor(diff / 60)}m ago`
  if (diff < 86400) return `${Math.floor(diff / 3600)}h ago`
  return `${Math.floor(diff / 86400)}d ago`
}

/** Poll interval while the tab is visible (fast enough to feel immediate without WebSockets). */
const UNREAD_POLL_MS = 3000
const BANNER_AUTO_DISMISS_MS = 9000

export default function Navbar() {
  const { user, logout, roles } = useAuth()
  const navigate = useNavigate()

  const isClient = roles.some((r) => r.toUpperCase() === 'CLIENT')
  const isConsultant = roles.some((r) => r.toUpperCase() === 'CONSULTANT')
  const isAdmin = roles.some((r) => r.toUpperCase() === 'ADMIN')

  const [open, setOpen] = useState(false)
  const [notifications, setNotifications] = useState([])
  const [unreadCount, setUnreadCount] = useState(0)
  const [banner, setBanner] = useState(null)
  const dropdownRef = useRef(null)
  const pollRef = useRef(null)
  const prevUnreadRef = useRef(null)
  const bannerTimerRef = useRef(null)

  const clearBannerTimer = useCallback(() => {
    if (bannerTimerRef.current) {
      clearTimeout(bannerTimerRef.current)
      bannerTimerRef.current = null
    }
  }, [])

  const dismissBanner = useCallback(() => {
    clearBannerTimer()
    setBanner(null)
  }, [clearBannerTimer])

  const fetchUnread = useCallback(async () => {
    if (!user) return
    try {
      const res = await getUnreadCount()
      const count = res.data.count
      const prev = prevUnreadRef.current
      if (prev !== null && count > prev) {
        const delta = count - prev
        let subtitle = ''
        try {
          const listRes = await getNotifications()
          const latest = listRes.data?.[0]
          subtitle = typeof latest?.payload === 'string' ? latest.payload.slice(0, 160) : ''
        } catch {
          // ignore
        }
        clearBannerTimer()
        const title = delta === 1 ? 'New notification' : `${delta} new notifications`
        setBanner({ title, subtitle })
        bannerTimerRef.current = setTimeout(() => {
          setBanner(null)
          bannerTimerRef.current = null
        }, BANNER_AUTO_DISMISS_MS)
      }
      prevUnreadRef.current = count
      setUnreadCount(count)
    } catch {
      // ignore
    }
  }, [user, clearBannerTimer])

  const fetchNotifications = useCallback(async () => {
    try {
      const res = await getNotifications()
      setNotifications(res.data)
    } catch {
      // ignore
    }
  }, [])

  useEffect(() => {
    prevUnreadRef.current = null
  }, [user])

  // Poll unread count so new notifications can show a banner quickly for any role
  useEffect(() => {
    if (!user) return
    fetchUnread()
    pollRef.current = setInterval(() => {
      if (!document.hidden) fetchUnread()
    }, UNREAD_POLL_MS)
    return () => {
      clearInterval(pollRef.current)
    }
  }, [user, fetchUnread])

  useEffect(() => {
    if (!user) return
    const onVisible = () => {
      if (!document.hidden) fetchUnread()
    }
    document.addEventListener('visibilitychange', onVisible)
    return () => document.removeEventListener('visibilitychange', onVisible)
  }, [user, fetchUnread])

  useEffect(() => () => clearBannerTimer(), [clearBannerTimer])

  // Close dropdown on outside click
  useEffect(() => {
    const handler = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setOpen(false)
      }
    }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const handleBellClick = async () => {
    if (!open) {
      await fetchNotifications()
    }
    setOpen((v) => !v)
  }

  const handleBannerClick = async () => {
    await fetchNotifications()
    setOpen(true)
    dismissBanner()
  }

  const handleMarkRead = async (id) => {
    await markNotificationRead(id)
    setNotifications((prev) =>
      prev.map((n) => (n.id === id ? { ...n, isRead: true } : n))
    )
    setUnreadCount((c) => Math.max(0, c - 1))
  }

  const handleMarkAllRead = async () => {
    await markAllNotificationsRead()
    setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })))
    setUnreadCount(0)
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="sticky top-0 z-50">
    <nav className="bg-[#1F2023] border-b border-[#2e303a] px-6 py-3 flex items-center justify-between">
      <Link to="/" className="text-xl font-semibold text-indigo-400">
        ConsultHub
      </Link>

      <div className="flex items-center gap-6">
        {isClient && (
          <Link to="/services" className="text-sm text-gray-400 hover:text-white transition-colors">Browse</Link>
        )}
        {isClient && (
          <>
            <Link to="/client/bookings" className="text-sm text-gray-400 hover:text-white transition-colors">My Bookings</Link>
            <Link to="/client/payments" className="text-sm text-gray-400 hover:text-white transition-colors">Payments</Link>
          </>
        )}
        {isConsultant && (
          <>
            <Link to="/consultant/dashboard" className="text-sm text-gray-400 hover:text-white transition-colors">Dashboard</Link>
            <Link to="/consultant/availability" className="text-sm text-gray-400 hover:text-white transition-colors">Availability</Link>
            <Link to="/consultant/bookings" className="text-sm text-gray-400 hover:text-white transition-colors">Bookings</Link>
          </>
        )}
        {isAdmin && (
          <>
            <Link to="/admin/approvals" className="text-sm text-gray-400 hover:text-white transition-colors">Approvals</Link>
            <Link to="/admin/services" className="text-sm text-gray-400 hover:text-white transition-colors">Services</Link>
            <Link to="/admin/policies" className="text-sm text-gray-400 hover:text-white transition-colors">Policies</Link>
            <Link to="/admin/status" className="text-sm text-gray-400 hover:text-white transition-colors">Status</Link>
          </>
        )}
      </div>

      {user && (
        <div className="flex items-center gap-3">
          {/* Notification Bell */}
          <div className="relative" ref={dropdownRef}>
            <button
              onClick={handleBellClick}
              className="relative p-2 rounded-lg text-gray-400 hover:text-white hover:bg-[#2e303a] transition-colors"
              aria-label="Notifications"
            >
              <Bell className="w-5 h-5" />
              {unreadCount > 0 && (
                <span className="absolute -top-0.5 -right-0.5 min-w-[18px] h-[18px] px-1 rounded-full bg-indigo-500 text-white text-[10px] font-bold flex items-center justify-center leading-none">
                  {unreadCount > 99 ? '99+' : unreadCount}
                </span>
              )}
            </button>

            {open && (
              <div className="absolute right-0 top-full mt-2 w-80 bg-[#1F2023] border border-[#2e303a] rounded-2xl shadow-2xl z-50 overflow-hidden">
                {/* Header */}
                <div className="flex items-center justify-between px-4 py-3 border-b border-[#2e303a]">
                  <span className="text-sm font-semibold text-white">Notifications</span>
                  {unreadCount > 0 && (
                    <button
                      onClick={handleMarkAllRead}
                      className="text-xs text-indigo-400 hover:text-indigo-300 transition-colors"
                    >
                      Mark all read
                    </button>
                  )}
                </div>

                {/* List */}
                <div className="max-h-80 overflow-y-auto divide-y divide-[#2e303a]">
                  {notifications.length === 0 ? (
                    <p className="text-sm text-gray-500 text-center py-8">No notifications yet.</p>
                  ) : (
                    notifications.map((n) => (
                      <div
                        key={n.id}
                        onClick={() => !n.isRead && handleMarkRead(n.id)}
                        className={`px-4 py-3 cursor-pointer transition-colors ${
                          n.isRead ? 'opacity-60' : 'hover:bg-[#2e303a]'
                        }`}
                      >
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex-1 min-w-0">
                            <span className={`text-[11px] font-semibold uppercase tracking-wide ${TYPE_COLOR[n.notificationType] ?? 'text-gray-400'}`}>
                              {TYPE_LABEL[n.notificationType] ?? n.notificationType}
                            </span>
                            <p className="text-sm text-gray-200 mt-0.5 leading-snug">{n.payload}</p>
                            <p className="text-xs text-gray-600 mt-1">{timeAgo(n.createdAt)}</p>
                          </div>
                          {!n.isRead && (
                            <span className="w-2 h-2 rounded-full bg-indigo-500 flex-shrink-0 mt-1.5" />
                          )}
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </div>
            )}
          </div>

          <span className="text-sm text-gray-400">{user.username || user.sub}</span>
          <button
            onClick={handleLogout}
            className="text-sm bg-[#2e303a] hover:bg-[#3a3c48] text-gray-300 hover:text-white px-3 py-1.5 rounded-lg transition-colors"
          >
            Logout
          </button>
        </div>
      )}
    </nav>

    <AnimatePresence>
      {banner && (
        <motion.div
          key="notification-banner"
          role="status"
          className="border-b border-indigo-500/35 bg-gradient-to-r from-indigo-950/95 to-[#1a1b24] px-4 py-2.5 flex items-start gap-3 shadow-lg overflow-hidden"
          initial={{ opacity: 0, y: -18, scale: 0.985 }}
          animate={{ opacity: 1, y: 0, scale: 1 }}
          exit={{
            opacity: 0,
            y: -10,
            scale: 0.99,
            transition: { duration: 0.32, ease: [0.4, 0, 1, 1] },
          }}
          transition={{
            type: 'spring',
            stiffness: 420,
            damping: 32,
            mass: 0.72,
            opacity: { duration: 0.3, ease: [0.22, 1, 0.36, 1] },
          }}
        >
          <button
            type="button"
            onClick={handleBannerClick}
            className="flex-1 min-w-0 text-left rounded-lg focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-400"
          >
            <p className="text-sm font-semibold text-white">{banner.title}</p>
            {banner.subtitle ? (
              <p className="text-xs text-gray-300 mt-0.5 line-clamp-2 leading-snug">{banner.subtitle}</p>
            ) : null}
            <p className="text-[11px] text-indigo-300/90 mt-1">Open notifications</p>
          </button>
          <button
            type="button"
            onClick={(e) => {
              e.stopPropagation()
              dismissBanner()
            }}
            className="p-1 rounded-lg text-gray-400 hover:text-white hover:bg-white/10 shrink-0"
            aria-label="Dismiss notification banner"
          >
            <X className="w-4 h-4" />
          </button>
        </motion.div>
      )}
    </AnimatePresence>
    </div>
  )
}
