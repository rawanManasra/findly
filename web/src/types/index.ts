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

// Time slot type
export interface TimeSlot {
  startTime: string // HH:mm
  endTime: string // HH:mm
  available: boolean
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
