// User types
export type UserRole = 'CUSTOMER' | 'BUSINESS_OWNER' | 'ADMIN'

export interface User {
  id: string
  email: string
  phone?: string
  firstName: string
  lastName?: string
  role: UserRole
  emailVerified: boolean
  phoneVerified: boolean
  avatarUrl?: string
  createdAt: string
}

// Business types
export type BusinessStatus = 'ACTIVE' | 'INACTIVE' | 'PENDING_APPROVAL' | 'SUSPENDED'

export interface Business {
  id: string
  ownerId: string
  name: string
  description?: string
  categoryId?: string
  category?: Category
  phone?: string
  email?: string
  website?: string
  imageUrl?: string
  addressLine1?: string
  addressLine2?: string
  city?: string
  state?: string
  postalCode?: string
  country: string
  latitude?: number
  longitude?: number
  distance?: number // Distance in meters from user
  status: BusinessStatus
  isVerified: boolean
  ratingAvg: number
  ratingCount: number
  createdAt: string
}

// Category types
export interface Category {
  id: string
  name: string
  slug: string
  icon?: string
  parentId?: string
  children?: Category[]
}

// Service types
export interface Service {
  id: string
  businessId: string
  name: string
  description?: string
  durationMins: number
  price?: number
  currency: string
  isActive: boolean
}

// Working hours types
export interface WorkingHours {
  id: string
  businessId: string
  dayOfWeek: number // 0-6, Sunday = 0
  startTime?: string // HH:mm
  endTime?: string // HH:mm
  isClosed: boolean
  breakStart?: string
  breakEnd?: string
}

// Booking types
export type BookingStatus = 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'COMPLETED' | 'NO_SHOW'

export interface Booking {
  id: string
  businessId: string
  business?: Business
  serviceId: string
  service?: Service
  customerId?: string
  customer?: User
  guestName?: string
  guestPhone?: string
  guestEmail?: string
  date: string // YYYY-MM-DD
  startTime: string // HH:mm
  endTime: string // HH:mm
  status: BookingStatus
  notes?: string
  rejectionReason?: string
  bookedAt: string
  confirmedAt?: string
  cancelledAt?: string
  completedAt?: string
}

// Time slot types (match backend TimeSlotResponse)
export interface TimeSlot {
  startTime: string // HH:mm
  endTime: string // HH:mm
  available: boolean
}

export interface TimeSlotResponse {
  date: string // YYYY-MM-DD
  businessOpen: boolean
  openTime: string | null
  closeTime: string | null
  slots: TimeSlot[]
}

// Booking response (match backend BookingResponse)
export interface BookingResponse {
  id: string
  businessId: string
  businessName: string
  businessPhone: string | null
  businessAddress: string | null
  serviceId: string
  serviceName: string
  serviceDurationMins: number
  servicePrice: string
  customerId: string | null
  customerName: string
  customerPhone: string
  customerEmail: string | null
  guestBooking: boolean
  date: string // YYYY-MM-DD
  startTime: string // HH:mm
  endTime: string // HH:mm
  status: BookingStatus
  notes: string | null
  rejectionReason: string | null
  bookedAt: string
  confirmedAt: string | null
  cancelledAt: string | null
  completedAt: string | null
  createdAt: string
}

// Business detail response (match backend BusinessDetailResponse)
export interface BusinessDetailResponse {
  id: string
  name: string
  description: string | null
  categoryId: string | null
  categoryName: string | null
  phone: string | null
  email: string | null
  website: string | null
  imageUrl: string | null
  address: string | null
  city: string | null
  state: string | null
  postalCode: string | null
  country: string
  latitude: number | null
  longitude: number | null
  status: BusinessStatus
  isVerified: boolean
  ratingAvg: number
  ratingCount: number
  createdAt: string
  services: ServiceResponse[]
  workingHours: WorkingHoursResponse[]
}

// Service response (match backend)
export interface ServiceResponse {
  id: string
  name: string
  description: string | null
  durationMins: number
  formattedPrice: string
  price: number | null
  currency: string
  active: boolean
  sortOrder: number
}

// Working hours response (match backend)
export interface WorkingHoursResponse {
  id: string
  dayOfWeek: number
  dayName: string
  startTime: string | null
  endTime: string | null
  closed: boolean
  breakStart: string | null
  breakEnd: string | null
}

// Auth types
export interface AuthResponse {
  accessToken: string
  refreshToken: string
  user: User
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
  firstName: string
  lastName?: string
  phone?: string
  role: 'CUSTOMER' | 'BUSINESS_OWNER'
}

// API response types
export interface PaginatedResponse<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

export interface ApiError {
  message: string
  code?: string
  details?: Record<string, string>
}
