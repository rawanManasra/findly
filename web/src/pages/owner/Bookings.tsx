import { useEffect, useState } from 'react'
import OwnerLayout from '@/components/owner/OwnerLayout'
import { ownerApi } from '@/services/api'
import { BookingResponse, BookingStatus } from '@/types'

const statusFilters: { value: string; label: string }[] = [
  { value: '', label: 'All Bookings' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'NO_SHOW', label: 'No Show' },
]

export default function Bookings() {
  const [bookings, setBookings] = useState<BookingResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [statusFilter, setStatusFilter] = useState('')
  const [dateFilter, setDateFilter] = useState('')

  useEffect(() => {
    loadBookings()
  }, [statusFilter, dateFilter])

  const loadBookings = async () => {
    try {
      setLoading(true)
      setError(null)

      const params: { status?: string; date?: string } = {}
      if (statusFilter) params.status = statusFilter
      if (dateFilter) params.date = dateFilter

      const response = await ownerApi.getBookings(params)
      setBookings(response.data.content || response.data || [])
    } catch (err) {
      console.error('Failed to load bookings:', err)
      setError('Failed to load bookings')
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async (bookingId: string) => {
    try {
      await ownerApi.approveBooking(bookingId)
      loadBookings()
    } catch (err) {
      console.error('Failed to approve booking:', err)
    }
  }

  const handleReject = async (bookingId: string) => {
    const reason = prompt('Rejection reason (optional):')
    try {
      await ownerApi.rejectBooking(bookingId, reason || undefined)
      loadBookings()
    } catch (err) {
      console.error('Failed to reject booking:', err)
    }
  }

  const handleComplete = async (bookingId: string) => {
    try {
      await ownerApi.completeBooking(bookingId)
      loadBookings()
    } catch (err) {
      console.error('Failed to complete booking:', err)
    }
  }

  const handleNoShow = async (bookingId: string) => {
    try {
      await ownerApi.markNoShow(bookingId)
      loadBookings()
    } catch (err) {
      console.error('Failed to mark no-show:', err)
    }
  }

  const formatTime = (time: string) => {
    const [hours, minutes] = time.split(':')
    const h = parseInt(hours)
    const ampm = h >= 12 ? 'PM' : 'AM'
    const h12 = h % 12 || 12
    return `${h12}:${minutes} ${ampm}`
  }

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr)
    return date.toLocaleDateString('en-US', {
      weekday: 'short',
      month: 'short',
      day: 'numeric',
    })
  }

  const getStatusBadge = (status: BookingStatus) => {
    const styles: Record<BookingStatus, string> = {
      PENDING: 'bg-yellow-100 text-yellow-800',
      APPROVED: 'bg-green-100 text-green-800',
      COMPLETED: 'bg-blue-100 text-blue-800',
      CANCELLED: 'bg-gray-100 text-gray-800',
      REJECTED: 'bg-red-100 text-red-800',
      NO_SHOW: 'bg-orange-100 text-orange-800',
    }
    return styles[status] || 'bg-gray-100 text-gray-800'
  }

  const getActions = (booking: BookingResponse) => {
    switch (booking.status) {
      case 'PENDING':
        return (
          <>
            <button
              onClick={() => handleApprove(booking.id)}
              className="px-3 py-1 bg-green-600 text-white text-sm rounded hover:bg-green-700"
            >
              Approve
            </button>
            <button
              onClick={() => handleReject(booking.id)}
              className="px-3 py-1 bg-red-100 text-red-700 text-sm rounded hover:bg-red-200"
            >
              Reject
            </button>
          </>
        )
      case 'APPROVED':
        return (
          <>
            <button
              onClick={() => handleComplete(booking.id)}
              className="px-3 py-1 bg-blue-600 text-white text-sm rounded hover:bg-blue-700"
            >
              Complete
            </button>
            <button
              onClick={() => handleNoShow(booking.id)}
              className="px-3 py-1 bg-orange-100 text-orange-700 text-sm rounded hover:bg-orange-200"
            >
              No Show
            </button>
          </>
        )
      default:
        return null
    }
  }

  return (
    <OwnerLayout>
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-2xl font-bold text-gray-900">Bookings</h2>
      </div>

      {/* Filters */}
      <div className="card p-4 mb-6">
        <div className="flex flex-wrap gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Status</label>
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="input-field w-48"
            >
              {statusFilters.map((filter) => (
                <option key={filter.value} value={filter.value}>
                  {filter.label}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Date</label>
            <input
              type="date"
              value={dateFilter}
              onChange={(e) => setDateFilter(e.target.value)}
              className="input-field w-48"
            />
          </div>
          {(statusFilter || dateFilter) && (
            <div className="flex items-end">
              <button
                onClick={() => {
                  setStatusFilter('')
                  setDateFilter('')
                }}
                className="px-4 py-2 text-gray-600 hover:text-gray-900"
              >
                Clear Filters
              </button>
            </div>
          )}
        </div>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {/* Bookings Table */}
      <div className="card overflow-hidden">
        {loading ? (
          <div className="flex items-center justify-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
          </div>
        ) : bookings.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Customer
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Service
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Date & Time
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {bookings.map((booking) => (
                  <tr key={booking.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <p className="font-medium text-gray-900">{booking.customerName}</p>
                        <p className="text-sm text-gray-500">{booking.customerPhone}</p>
                        {booking.guestBooking && (
                          <span className="inline-block px-2 py-0.5 text-xs bg-gray-100 text-gray-600 rounded">
                            Guest
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <p className="text-gray-900">{booking.serviceName}</p>
                        <p className="text-sm text-gray-500">
                          {booking.serviceDurationMins} min • {booking.servicePrice}
                        </p>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div>
                        <p className="text-gray-900">{formatDate(booking.date)}</p>
                        <p className="text-sm text-gray-500">
                          {formatTime(booking.startTime)} - {formatTime(booking.endTime)}
                        </p>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span
                        className={`px-3 py-1 rounded-full text-sm font-medium ${getStatusBadge(
                          booking.status
                        )}`}
                      >
                        {booking.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex gap-2">{getActions(booking)}</div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="p-6 text-center text-gray-500">
            No bookings found.
          </div>
        )}
      </div>
    </OwnerLayout>
  )
}
