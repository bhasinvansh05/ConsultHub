import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './auth/context/AuthContext'
import ProtectedRoute from './shared/components/ProtectedRoute'
import ConsultantRoute from './shared/components/ConsultantRoute'
import Navbar from './shared/components/Navbar'

import Login from './auth/pages/Login'
import Register from './auth/pages/Register'

import ClientServices from './client/pages/Services'
import Book from './client/pages/Book'
import ClientBookings from './client/pages/Bookings'
import ClientPayments from './client/pages/Payments'

import ConsultantDashboard from './consultant/pages/Dashboard'
import ConsultantAvailability from './consultant/pages/Availability'
import ConsultantBookings from './consultant/pages/Bookings'

import AdminApprovals from './admin/pages/Approvals'
import AdminPolicies from './admin/pages/Policies'
import AdminServices from './admin/pages/Services'
import SystemStatus from './admin/pages/SystemStatus'
import ChatWidget from './shared/components/ChatWidget'
import Landing from './pages/Landing'

function RootRedirect() {
  const { isLoggedIn, roles } = useAuth()
  if (!isLoggedIn) return <Landing />
  const role = roles[0]?.toUpperCase()
  if (role === 'ADMIN') return <Navigate to="/admin/status" replace />
  if (role === 'CONSULTANT') return <Navigate to="/consultant/dashboard" replace />
  return <Navigate to="/client/services" replace />
}

function Layout({ children }) {
  const { isLoggedIn } = useAuth()
  return (
    <div className="min-h-screen bg-[#16171d]">
      {isLoggedIn && <Navbar />}
      <main>{children}</main>
      {isLoggedIn && <ChatWidget />}
    </div>
  )
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Layout>
          <Routes>
            <Route path="/" element={<RootRedirect />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/unauthorized" element={<div className="p-8 text-center text-red-500">Access denied.</div>} />

            {/* Public */}
            <Route path="/services" element={<ClientServices />} />

            {/* Client */}
            <Route path="/client/services" element={<ClientServices />} />
            <Route path="/client/book" element={<ProtectedRoute role="CLIENT"><Book /></ProtectedRoute>} />
            <Route path="/client/bookings" element={<ProtectedRoute role="CLIENT"><ClientBookings /></ProtectedRoute>} />
            <Route path="/client/payments" element={<ProtectedRoute role="CLIENT"><ClientPayments /></ProtectedRoute>} />

            {/* Consultant */}
            <Route path="/consultant/services" element={<Navigate to="/consultant/availability" replace />} />
            <Route path="/consultant/dashboard" element={<ConsultantRoute><ConsultantDashboard /></ConsultantRoute>} />
            <Route path="/consultant/availability" element={<ConsultantRoute><ConsultantAvailability /></ConsultantRoute>} />
            <Route path="/consultant/bookings" element={<ConsultantRoute><ConsultantBookings /></ConsultantRoute>} />

            {/* Admin */}
            <Route path="/admin/approvals" element={<ProtectedRoute role="ADMIN"><AdminApprovals /></ProtectedRoute>} />
            <Route path="/admin/policies" element={<ProtectedRoute role="ADMIN"><AdminPolicies /></ProtectedRoute>} />
            <Route path="/admin/services" element={<ProtectedRoute role="ADMIN"><AdminServices /></ProtectedRoute>} />
            <Route path="/admin/status" element={<ProtectedRoute role="ADMIN"><SystemStatus /></ProtectedRoute>} />

            <Route path="/chatbot" element={<Navigate to="/" replace />} />

<Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </Layout>
      </AuthProvider>
    </BrowserRouter>
  )
}
