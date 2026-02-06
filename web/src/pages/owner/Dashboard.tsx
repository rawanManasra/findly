import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import OwnerLayout from '@/components/owner/OwnerLayout'
import { ownerApi } from '@/services/api'
import { BookingResponse, Business } from '@/types'

export default function Dashboard() {
  const [businesses, setBusinesses] = useState<Business[]>([])
  const [todayBookings, setTodayBookings] = useState<BookingResponse[]>([])
  const [pendingBookings, setPendingBookings] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadDashboardData()
  }, [])

  const loadDashboardData = async () => {
    try {
      setLoading(true)
      setError(null)

      const [businessesRes, todayRes, pendingRes] = await Promise.all([
        ownerApi.getMyBusinesses(),
        ownerApi.getTodayBookings(),
        ownerApi.getPendingBookings(),
      ])

      setBusinesses(businessesRes.data.content || businessesRes.data || [])
      setTodayBookings(todayRes.data.content || todayRes.data || [])
      setPendingBookings(pendingRes.data.content || pendingRes.data || [])
    } catch (err: unknown) {
      console.error('Failed to load dashboard:', err)
      setError('Failed to load dashboard data')
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (bookingId: string) => {
    try {
      await ownerApi.approveBooking(bookingId)
      loadDashboardData()
    } catch (err) {
      console.error('Failed to approve booking:', err)
    }
  }

  const handleReject = async (bookingId: string) => {
    const reason = prompt('Rejection reason (optional):')
    try {
      await ownerApi.rejectBooking(bookingId, reason || undefined)
      loadDashboardData()
    } catch (err) {
      console.error('Failed to reject booking:', err)
    }
  }

  const formatTime = (time: string) => {
    const [hours, minutes] = time.split(':')
    const h = parseInt(hours)
    const ampm = h >= 12 ? 'PM' : 'AM'
    const h12 = h % 12 || 12
    return `${h12}:${minutes} ${ampm}`
  }

  if (loading) {
    return (
      <OwnerLayout>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
        </div>
      </OwnerLayout>
    )
  }

  return (
    <OwnerLayout>
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-2xl font-bold text-gray-900">Dashboard</h2>
        {businesses.length === 0 && (
          <Link to="/owner/settings" className="btn-primary">
            + Create Business
          </Link>
        )}
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="card p-6">
          <p className="text-sm text-gray-500">Today's Bookings</p>
          <p className="text-3xl font-bold text-gray-900">{todayBookings.length}</p>
        </div>
        <div className="card p-6">
          <p className="text-sm text-gray-500">Pending Approval</p>
          <p className="text-3xl font-bold text-yellow-600">{pendingBookings.length}</p>
        </div>
        <div className="card p-6">
          <p className="text-sm text-gray-500">Businesses</p>
          <p className="text-3xl font-bold text-gray-900">{businesses.length}</p>
        </div>
        <div className="card p-6">
          <p className="text-sm text-gray-500">Active Services</p>
          <p className="text-3xl font-bold text-gray-900">
            {businesses.reduce((acc, b) => acc + (b.ratingCount || 0), 0)}
          </p>
        </div>
      </div>

      {/* Pending Bookings */}
      {pendingBookings.length > 0 && (
        <div className="card mb-8">
          <div className="px-6 py-4 border-b bg-yellow-50">
            <h3 className="text-lg font-semibold text-yellow-800">
              Pending Approval ({pendingBookings.length})
            </h3>
          </div>
          <div className="divide-y">
            {pendingBookings.map((booking) => (
              <div key={booking.id} className="p-6 flex items-center justify-between">
                <div>
                  <p className="font-medium text-gray-900">{booking.customerName}</p>
                  <p className="text-sm text-gray-500">
                    {booking.serviceName} • {booking.date} at {formatTime(booking.startTime)}
                  </p>
                  {booking.customerPhone && (
                    <p className="text-sm text-gray-500">{booking.customerPhone}</p>
                  )}
                </div>
                <div className="flex gap-2">
                  <button
                    onClick={() => handleApprove(booking.id)}
                    className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors"
                  >
                    Approve
                  </button>
                  <button
                    onClick={() => handleReject(booking.id)}
                    className="px-4 py-2 bg-red-100 text-red-700 rounded-lg hover:bg-red-200 transition-colors"
                  >
                    Reject
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Today's Bookings */}
      <div className="card">
        <div className="px-6 py-4 border-b">
          <h3 className="text-lg font-semibold text-gray-900">Today's Schedule</h3>
        </div>
        {todayBookings.length > 0 ? (
          <div className="divide-y">
            {todayBookings.map((booking) => (
              <div key={booking.id} className="p-6 flex items-center justify-between">
                <div className="flex items-center gap-4">
                  <div className="text-center min-w-[60px]">
                    <p className="text-lg font-bold text-primary-600">
                      {formatTime(booking.startTime)}
                    </p>
                  </div>
                  <div>
                    <p className="font-medium text-gray-900">{booking.customerName}</p>
                    <p className="text-sm text-gray-500">{booking.serviceName}</p>
                  </div>
                </div>
                <span
                  className={`px-3 py-1 rounded-full text-sm font-medium ${
                    booking.status === 'APPROVED'
                      ? 'bg-green-100 text-green-800'
                      : booking.status === 'PENDING'
                      ? 'bg-yellow-100 text-yellow-800'
                      : booking.status === 'COMPLETED'
                      ? 'bg-blue-100 text-blue-800'
                      : 'bg-gray-100 text-gray-800'
                  }`}
                >
                  {booking.status}
                </span>
              </div>
            ))}
          </div>
        ) : (
          <div className="p-6">
            <p className="text-gray-500 text-center py-8">
              No bookings scheduled for today.
            </p>
          </div>
        )}
      </div>

      {/* Quick Links */}
      <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
        <Link
          to="/owner/bookings"
          className="card p-6 hover:shadow-md transition-shadow text-center"
        >
          <span className="text-3xl mb-2 block">📅</span>
          <p className="font-medium text-gray-900">View All Bookings</p>
        </Link>
        <Link
          to="/owner/services"
          className="card p-6 hover:shadow-md transition-shadow text-center"
        >
          <span className="text-3xl mb-2 block">🛠</span>
          <p className="font-medium text-gray-900">Manage Services</p>
        </Link>
        <Link
          to="/owner/hours"
          className="card p-6 hover:shadow-md transition-shadow text-center"
        >
          <span className="text-3xl mb-2 block">🕐</span>
          <p className="font-medium text-gray-900">Set Working Hours</p>
        </Link>
      </div>
    </OwnerLayout>
  )
}
