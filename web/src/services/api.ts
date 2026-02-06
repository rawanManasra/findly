import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'

export const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor for token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      try {
        const refreshToken = localStorage.getItem('refreshToken')
        const response = await axios.post(`${API_BASE_URL}/auth/refresh`, {
          refreshToken,
        })

        const { accessToken } = response.data
        localStorage.setItem('accessToken', accessToken)

        originalRequest.headers.Authorization = `Bearer ${accessToken}`
        return api(originalRequest)
      } catch (refreshError) {
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        window.location.href = '/login'
        return Promise.reject(refreshError)
      }
    }

    return Promise.reject(error)
  }
)

// Auth API
export const authApi = {
  login: (email: string, password: string) =>
    api.post('/auth/login', { email, password }),

  register: (data: {
    email: string
    password: string
    firstName: string
    lastName?: string
    phone?: string
    role: 'CUSTOMER' | 'BUSINESS_OWNER'
  }) => api.post('/auth/register', data),

  logout: () => api.post('/auth/logout'),

  me: () => api.get('/auth/me'),
}

// Business API
export const businessApi = {
  search: (params: {
    lat: number
    lng: number
    radius?: number
    category?: string
    q?: string
    page?: number
    size?: number
  }) => api.get('/businesses', { params }),

  getById: (id: string) => api.get(`/businesses/${id}`),

  getServices: (id: string) => api.get(`/businesses/${id}/services`),

  getWorkingHours: (id: string) => api.get(`/businesses/${id}/hours`),

  getSlots: (businessId: string, serviceId: string, date: string) =>
    api.get(`/bookings/businesses/${businessId}/slots`, { params: { serviceId, date } }),
}

// Category API
export const categoryApi = {
  getAll: () => api.get('/categories'),
  getById: (id: string) => api.get(`/categories/${id}`),
  getBySlug: (slug: string) => api.get(`/categories/slug/${slug}`),
}

// Booking API
export const bookingApi = {
  create: (data: {
    businessId: string
    serviceId: string
    date: string
    startTime: string
    notes?: string
  }) => api.post('/bookings', data),

  createGuest: (data: {
    businessId: string
    serviceId: string
    date: string
    startTime: string
    notes?: string
    guestName: string
    guestPhone: string
    guestEmail?: string
  }) => api.post('/bookings/guest', data),

  getMyBookings: (status?: string) => api.get('/bookings', { params: { status } }),

  getById: (id: string) => api.get(`/bookings/${id}`),

  cancel: (id: string) => api.put(`/bookings/${id}/cancel`),
}

// Owner API
export const ownerApi = {
  // Business management
  getMyBusinesses: () => api.get('/owner/businesses'),
  getBusiness: (id: string) => api.get(`/owner/businesses/${id}`),
  createBusiness: (data: {
    name: string
    description?: string
    categoryId?: string
    phone?: string
    email?: string
    website?: string
  }) => api.post('/owner/businesses', data),
  updateBusiness: (id: string, data: {
    name?: string
    description?: string
    categoryId?: string
    phone?: string
    email?: string
    website?: string
  }) => api.put(`/owner/businesses/${id}`, data),
  updateLocation: (id: string, data: {
    addressLine1: string
    addressLine2?: string
    city: string
    state?: string
    postalCode?: string
    country: string
    latitude: number
    longitude: number
  }) => api.put(`/owner/businesses/${id}/location`, data),
  deleteBusiness: (id: string) => api.delete(`/owner/businesses/${id}`),

  // Services management
  getServices: (businessId: string) => api.get(`/owner/businesses/${businessId}/services`),
  addService: (businessId: string, data: {
    name: string
    description?: string
    durationMins: number
    price?: number
    currency?: string
  }) => api.post(`/owner/businesses/${businessId}/services`, data),
  updateService: (businessId: string, serviceId: string, data: {
    name?: string
    description?: string
    durationMins?: number
    price?: number
    currency?: string
    active?: boolean
  }) => api.put(`/owner/businesses/${businessId}/services/${serviceId}`, data),
  deleteService: (businessId: string, serviceId: string) =>
    api.delete(`/owner/businesses/${businessId}/services/${serviceId}`),

  // Working hours management
  getWorkingHours: (businessId: string) => api.get(`/owner/businesses/${businessId}/hours`),
  updateWorkingHours: (businessId: string, data: {
    hours: Array<{
      dayOfWeek: number
      startTime?: string
      endTime?: string
      closed: boolean
      breakStart?: string
      breakEnd?: string
    }>
  }) => api.put(`/owner/businesses/${businessId}/hours`, data),

  // Booking management
  getBookings: (params?: { businessId?: string; status?: string; date?: string }) =>
    api.get('/owner/bookings', { params }),
  getBooking: (id: string) => api.get(`/owner/bookings/${id}`),
  getTodayBookings: (businessId?: string) =>
    api.get('/owner/bookings/today', { params: { businessId } }),
  getPendingBookings: (businessId?: string) =>
    api.get('/owner/bookings/pending', { params: { businessId } }),
  approveBooking: (id: string) => api.put(`/owner/bookings/${id}/approve`),
  rejectBooking: (id: string, reason?: string) =>
    api.put(`/owner/bookings/${id}/reject`, { reason }),
  completeBooking: (id: string) => api.put(`/owner/bookings/${id}/complete`),
  markNoShow: (id: string) => api.put(`/owner/bookings/${id}/no-show`),
}
