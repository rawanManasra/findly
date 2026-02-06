import { useState } from 'react'
import { useParams, Link, useNavigate } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useAuth } from '@/hooks/useAuth'
import { businessApi, bookingApi } from '@/services/api'
import { BusinessDetailResponse, ServiceResponse, TimeSlotResponse, TimeSlot } from '@/types'

const DAY_NAMES = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']

function formatDate(date: Date): string {
  return date.toISOString().split('T')[0]
}

function getNextDays(count: number): Date[] {
  const days: Date[] = []
  const today = new Date()
  for (let i = 0; i < count; i++) {
    const date = new Date(today)
    date.setDate(today.getDate() + i)
    days.push(date)
  }
  return days
}

export default function BusinessDetail() {
  const { id } = useParams<{ id: string }>()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { user, isAuthenticated } = useAuth()

  const [selectedService, setSelectedService] = useState<ServiceResponse | null>(null)
  const [selectedDate, setSelectedDate] = useState<Date>(new Date())
  const [selectedSlot, setSelectedSlot] = useState<TimeSlot | null>(null)
  const [bookingNotes, setBookingNotes] = useState('')
  const [showBookingModal, setShowBookingModal] = useState(false)

  // Guest booking fields
  const [guestName, setGuestName] = useState('')
  const [guestPhone, setGuestPhone] = useState('')
  const [guestEmail, setGuestEmail] = useState('')
  const [bookingError, setBookingError] = useState<string | null>(null)
  const [bookingSuccess, setBookingSuccess] = useState(false)

  // Fetch business details
  const { data: business, isLoading, error } = useQuery({
    queryKey: ['business', id],
    queryFn: async () => {
      const response = await businessApi.getById(id!)
      return response.data as BusinessDetailResponse
    },
    enabled: !!id,
  })

  // Fetch available slots when service and date are selected
  const { data: slotsData, isLoading: slotsLoading } = useQuery({
    queryKey: ['slots', id, selectedService?.id, formatDate(selectedDate)],
    queryFn: async () => {
      const response = await businessApi.getSlots(id!, selectedService!.id, formatDate(selectedDate))
      return response.data as TimeSlotResponse
    },
    enabled: !!id && !!selectedService,
  })

  // Create booking mutation
  const createBookingMutation = useMutation({
    mutationFn: async () => {
      if (isAuthenticated) {
        return bookingApi.create({
          businessId: id!,
          serviceId: selectedService!.id,
          date: formatDate(selectedDate),
          startTime: selectedSlot!.startTime,
          notes: bookingNotes || undefined,
        })
      } else {
        return bookingApi.createGuest({
          businessId: id!,
          serviceId: selectedService!.id,
          date: formatDate(selectedDate),
          startTime: selectedSlot!.startTime,
          notes: bookingNotes || undefined,
          guestName,
          guestPhone,
          guestEmail: guestEmail || undefined,
        })
      }
    },
    onSuccess: () => {
      setBookingSuccess(true)
      queryClient.invalidateQueries({ queryKey: ['slots'] })
    },
    onError: (err: any) => {
      setBookingError(err.response?.data?.message || 'Failed to create booking. Please try again.')
    },
  })

  const handleServiceSelect = (service: ServiceResponse) => {
    setSelectedService(service)
    setSelectedSlot(null)
    setShowBookingModal(true)
  }

  const handleBooking = () => {
    setBookingError(null)

    // Validate guest fields
    if (!isAuthenticated) {
      if (!guestName.trim()) {
        setBookingError('Please enter your name')
        return
      }
      if (!guestPhone.trim()) {
        setBookingError('Please enter your phone number')
        return
      }
    }

    if (!selectedSlot) {
      setBookingError('Please select a time slot')
      return
    }

    createBookingMutation.mutate()
  }

  const closeModal = () => {
    setShowBookingModal(false)
    setSelectedService(null)
    setSelectedSlot(null)
    setBookingNotes('')
    setGuestName('')
    setGuestPhone('')
    setGuestEmail('')
    setBookingError(null)
    setBookingSuccess(false)
  }

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white shadow-sm">
          <div className="max-w-7xl mx-auto px-4 py-4">
            <Link to="/" className="text-xl font-bold text-primary-600">Findly</Link>
          </div>
        </header>
        <main className="max-w-4xl mx-auto px-4 py-8">
          <div className="card animate-pulse">
            <div className="h-48 bg-gray-200"></div>
            <div className="p-6">
              <div className="h-6 bg-gray-200 rounded w-1/2 mb-4"></div>
              <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
              <div className="h-4 bg-gray-200 rounded w-1/2"></div>
            </div>
          </div>
        </main>
      </div>
    )
  }

  if (error || !business) {
    return (
      <div className="min-h-screen bg-gray-50">
        <header className="bg-white shadow-sm">
          <div className="max-w-7xl mx-auto px-4 py-4">
            <Link to="/" className="text-xl font-bold text-primary-600">Findly</Link>
          </div>
        </header>
        <main className="max-w-4xl mx-auto px-4 py-8 text-center">
          <h1 className="text-2xl font-bold text-gray-900 mb-4">Business not found</h1>
          <Link to="/search" className="btn-primary">Back to search</Link>
        </main>
      </div>
    )
  }

  const availableDays = getNextDays(14)
  const availableSlots = slotsData?.slots?.filter(s => s.available) || []

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center justify-between">
          <Link to="/" className="text-xl font-bold text-primary-600">Findly</Link>
          <button onClick={() => navigate(-1)} className="text-gray-600 hover:text-gray-900">
            &larr; Back
          </button>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-8">
        <div className="card overflow-hidden">
          {/* Business Header */}
          {business.imageUrl ? (
            <img src={business.imageUrl} alt={business.name} className="h-48 w-full object-cover" />
          ) : (
            <div className="h-48 bg-gradient-to-br from-primary-100 to-primary-200 flex items-center justify-center">
              <span className="text-6xl text-primary-400">{business.name.charAt(0).toUpperCase()}</span>
            </div>
          )}

          <div className="p-6">
            {/* Name and verification */}
            <div className="flex items-start justify-between mb-2">
              <h1 className="text-2xl font-bold text-gray-900">{business.name}</h1>
              {business.isVerified && (
                <span className="inline-flex items-center gap-1 text-primary-600 text-sm">
                  <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                  </svg>
                  Verified
                </span>
              )}
            </div>

            {/* Category and rating */}
            <div className="flex items-center gap-4 text-sm text-gray-500 mb-4">
              {business.categoryName && <span>{business.categoryName}</span>}
              {business.ratingCount > 0 && (
                <span className="flex items-center gap-1">
                  <svg className="w-4 h-4 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                  {business.ratingAvg.toFixed(1)} ({business.ratingCount} reviews)
                </span>
              )}
            </div>

            {/* Description */}
            {business.description && (
              <p className="text-gray-600 mb-6">{business.description}</p>
            )}

            {/* Contact info */}
            <div className="flex flex-wrap gap-4 text-sm text-gray-600 mb-6">
              {business.address && (
                <span className="flex items-center gap-1">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                  </svg>
                  {business.address}, {business.city}
                </span>
              )}
              {business.phone && (
                <a href={`tel:${business.phone}`} className="flex items-center gap-1 text-primary-600 hover:text-primary-700">
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                  {business.phone}
                </a>
              )}
            </div>

            {/* Services */}
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Services</h2>
            {business.services && business.services.length > 0 ? (
              <div className="space-y-3 mb-8">
                {business.services.filter(s => s.active).map((service) => (
                  <div key={service.id} className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                    <div>
                      <h3 className="font-medium text-gray-900">{service.name}</h3>
                      {service.description && (
                        <p className="text-sm text-gray-500 mt-1">{service.description}</p>
                      )}
                      <p className="text-sm text-gray-500 mt-1">{service.durationMins} mins</p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium text-gray-900">{service.formattedPrice}</p>
                      <button
                        onClick={() => handleServiceSelect(service)}
                        className="mt-2 text-sm btn-primary px-4 py-1"
                      >
                        Book
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-gray-500 mb-8">No services available</p>
            )}

            {/* Working Hours */}
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Working Hours</h2>
            {business.workingHours && business.workingHours.length > 0 ? (
              <div className="space-y-2 text-sm">
                {business.workingHours
                  .sort((a, b) => a.dayOfWeek - b.dayOfWeek)
                  .map((hours) => (
                    <div key={hours.id} className="flex justify-between">
                      <span className="text-gray-600">{hours.dayName}</span>
                      <span className="text-gray-900">
                        {hours.closed ? (
                          <span className="text-gray-400">Closed</span>
                        ) : (
                          `${hours.startTime} - ${hours.endTime}`
                        )}
                      </span>
                    </div>
                  ))}
              </div>
            ) : (
              <p className="text-gray-500">Working hours not available</p>
            )}
          </div>
        </div>
      </main>

      {/* Booking Modal */}
      {showBookingModal && selectedService && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
            {bookingSuccess ? (
              // Success state
              <div className="p-6 text-center">
                <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <h3 className="text-xl font-semibold text-gray-900 mb-2">Booking Requested!</h3>
                <p className="text-gray-600 mb-6">
                  Your booking request has been sent. The business will confirm your appointment soon.
                </p>
                <button onClick={closeModal} className="btn-primary w-full py-3">
                  Done
                </button>
              </div>
            ) : (
              // Booking form
              <div className="p-6">
                <div className="flex justify-between items-center mb-4">
                  <h3 className="text-lg font-semibold text-gray-900">Book {selectedService.name}</h3>
                  <button onClick={closeModal} className="text-gray-400 hover:text-gray-600">
                    <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>

                <div className="text-sm text-gray-600 mb-4">
                  <span>{selectedService.durationMins} mins</span>
                  <span className="mx-2">•</span>
                  <span>{selectedService.formattedPrice}</span>
                </div>

                {/* Date selection */}
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Select Date</label>
                  <div className="flex gap-2 overflow-x-auto pb-2">
                    {availableDays.map((date) => {
                      const isSelected = formatDate(date) === formatDate(selectedDate)
                      const dayName = DAY_NAMES[date.getDay()].slice(0, 3)
                      const dayNum = date.getDate()
                      return (
                        <button
                          key={formatDate(date)}
                          onClick={() => {
                            setSelectedDate(date)
                            setSelectedSlot(null)
                          }}
                          className={`flex flex-col items-center px-3 py-2 rounded-lg border min-w-[60px] ${
                            isSelected
                              ? 'border-primary-500 bg-primary-50 text-primary-700'
                              : 'border-gray-200 hover:border-gray-300'
                          }`}
                        >
                          <span className="text-xs">{dayName}</span>
                          <span className="text-lg font-semibold">{dayNum}</span>
                        </button>
                      )
                    })}
                  </div>
                </div>

                {/* Time slots */}
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-2">Select Time</label>
                  {slotsLoading ? (
                    <div className="flex gap-2 flex-wrap">
                      {[1, 2, 3, 4].map((i) => (
                        <div key={i} className="h-10 w-20 bg-gray-100 rounded animate-pulse"></div>
                      ))}
                    </div>
                  ) : !slotsData?.businessOpen ? (
                    <p className="text-gray-500 text-sm">Business is closed on this day</p>
                  ) : availableSlots.length === 0 ? (
                    <p className="text-gray-500 text-sm">No available slots for this day</p>
                  ) : (
                    <div className="flex gap-2 flex-wrap">
                      {availableSlots.map((slot) => {
                        const isSelected = selectedSlot?.startTime === slot.startTime
                        return (
                          <button
                            key={slot.startTime}
                            onClick={() => setSelectedSlot(slot)}
                            className={`px-4 py-2 rounded-lg border text-sm ${
                              isSelected
                                ? 'border-primary-500 bg-primary-50 text-primary-700'
                                : 'border-gray-200 hover:border-gray-300'
                            }`}
                          >
                            {slot.startTime}
                          </button>
                        )
                      })}
                    </div>
                  )}
                </div>

                {/* Guest info (if not logged in) */}
                {!isAuthenticated && (
                  <div className="space-y-3 mb-4 p-4 bg-gray-50 rounded-lg">
                    <p className="text-sm text-gray-600 mb-2">
                      Book as guest or <Link to={`/login?redirect=/business/${id}`} className="text-primary-600 hover:underline">login</Link> for a better experience
                    </p>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Your Name *</label>
                      <input
                        type="text"
                        value={guestName}
                        onChange={(e) => setGuestName(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                        placeholder="Enter your name"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Phone Number *</label>
                      <input
                        type="tel"
                        value={guestPhone}
                        onChange={(e) => setGuestPhone(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                        placeholder="Enter your phone"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Email (optional)</label>
                      <input
                        type="email"
                        value={guestEmail}
                        onChange={(e) => setGuestEmail(e.target.value)}
                        className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                        placeholder="Enter your email"
                      />
                    </div>
                  </div>
                )}

                {/* Notes */}
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 mb-1">Notes (optional)</label>
                  <textarea
                    value={bookingNotes}
                    onChange={(e) => setBookingNotes(e.target.value)}
                    rows={2}
                    className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary-500 focus:border-transparent"
                    placeholder="Any special requests?"
                  />
                </div>

                {/* Error message */}
                {bookingError && (
                  <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg text-sm text-red-600">
                    {bookingError}
                  </div>
                )}

                {/* Submit button */}
                <button
                  onClick={handleBooking}
                  disabled={!selectedSlot || createBookingMutation.isPending}
                  className="w-full btn-primary py-3 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {createBookingMutation.isPending ? (
                    <span className="flex items-center justify-center gap-2">
                      <span className="animate-spin h-4 w-4 border-2 border-white border-t-transparent rounded-full"></span>
                      Booking...
                    </span>
                  ) : (
                    'Confirm Booking'
                  )}
                </button>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  )
}
