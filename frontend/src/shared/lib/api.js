import axios from 'axios'

const api = axios.create({ baseURL: '' })

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401 && !err.config?.url?.includes('/api/auth/login')) {
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

// Auth
export const login = (email, password) =>
  api.post('/api/auth/login', { usernameOrEmail: email, password })

export const register = (data) =>
  api.post('/api/auth/register', data)

// Users
export const getUsers = () => api.get('/api/users')
export const getUser = (id) => api.get(`/api/users/${id}`)
export const updateUser = (id, data) => api.put(`/api/users/${id}`, data)

// Services
export const getServices = (serviceType) =>
  api.get('/api/services', serviceType ? { params: { serviceType } } : undefined)

export const getServiceById = (id) => api.get(`/api/services/${id}`)

export const createService = (consultantId, data) =>
  api.post(`/api/consultant/${consultantId}/services`, data)

export const getConsultantRegistrationStatus = (consultantId) =>
  api.get(`/api/consultant/${consultantId}/registration-status`)

export const getAvailabilityByService = (serviceId) =>
  api.get(`/api/services/${serviceId}/availability`)

// Availability
export const getAvailability = (consultantId) =>
  api.get(`/api/consultant/${consultantId}/availability`)

export const createSlot = (consultantId, data) =>
  api.post(`/api/consultant/${consultantId}/availability`, data)

export const deleteSlot = (consultantId, slotId) =>
  api.delete(`/api/consultant/${consultantId}/availability/${slotId}`)

// Bookings
export const createBooking = (data) => api.post('/bookings', data)
export const getBooking = (id) => api.get(`/bookings/${id}`)
export const getClientBookings = (clientId) => api.get(`/bookings/client/${clientId}`)
export const cancelBooking = (id) => api.put(`/bookings/${id}/cancel`)

export const getConsultantBookings = (consultantId, status) =>
  api.get(`/api/consultant/${consultantId}/bookings`, status ? { params: { status } } : undefined)

export const acceptBooking = (consultantId, bookingId) =>
  api.put(`/api/consultant/${consultantId}/bookings/${bookingId}/accept`)

export const rejectBooking = (consultantId, bookingId, reason) =>
  api.put(`/api/consultant/${consultantId}/bookings/${bookingId}/reject`,
    reason ? { reason } : {}
  )

export const completeBooking = (consultantId, bookingId) =>
  api.put(`/api/consultant/${consultantId}/bookings/${bookingId}/complete`)

// Payments
export const processPayment = (data) => api.post('/api/payments/process', data)
export const getPaymentMethods = (clientId) => api.get(`/api/payments/methods/${clientId}`)
export const addPaymentMethod = (clientId, data) => api.post(`/api/payments/methods/${clientId}`, data)
export const deletePaymentMethod = (clientId, id) => api.delete(`/api/payments/methods/${clientId}/${id}`)
export const getPaymentHistory = (clientId) => api.get(`/api/payments/history/${clientId}`)

// Notifications
export const getNotifications = () => api.get('/api/notifications')
export const getUnreadCount = () => api.get('/api/notifications/unread-count')
export const markNotificationRead = (id) => api.put(`/api/notifications/${id}/read`)
export const markAllNotificationsRead = () => api.put('/api/notifications/read-all')

// Admin
export const getSystemStatus = () => api.get('/api/admin/system/status')
export const getAdminStats = () => api.get('/api/admin/stats')
export const getPolicy = (key) => api.get(`/api/admin/policies/${key}`)
export const getPendingConsultants = () => api.get('/api/admin/consultants/pending')
export const approveConsultant = (consultantId, data) =>
  api.post(`/api/admin/consultants/${consultantId}/approval`, data)
export const updatePolicy = (key, data) => api.put(`/api/admin/policies/${key}`, data)
export const getAdminServices = () => api.get('/api/admin/services')
export const createAdminService = (data) => api.post('/api/admin/services', data)
export const updateAdminService = (id, data) => api.put(`/api/admin/services/${id}`, data)
export const deleteAdminService = (id) => api.delete(`/api/admin/services/${id}`)
