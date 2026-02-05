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

  getSlots: (id: string, date: string, serviceId?: string) =>
    api.get(`/businesses/${id}/slots`, { params: { date, service_id: serviceId } }),
}

// Booking API
export const bookingApi = {
  create: (data: {
    businessId: string
    serviceId: string
    date: string
    startTime: string
    notes?: string
    guestName?: string
    guestPhone?: string
    guestEmail?: string
  }) => api.post('/bookings', data),

  getMyBookings: () => api.get('/bookings'),

  cancel: (id: string) => api.put(`/bookings/${id}/cancel`),
}

// Owner API
export const ownerApi = {
  getMyBusinesses: () => api.get('/owner/businesses'),

  createBusiness: (data: any) => api.post('/owner/businesses', data),

  updateBusiness: (id: string, data: any) => api.put(`/owner/businesses/${id}`, data),

  getBookings: (params?: { businessId?: string; status?: string; date?: string }) =>
    api.get('/owner/bookings', { params }),

  approveBooking: (id: string) => api.put(`/owner/bookings/${id}/approve`),

  rejectBooking: (id: string, reason?: string) =>
    api.put(`/owner/bookings/${id}/reject`, { reason }),
}
