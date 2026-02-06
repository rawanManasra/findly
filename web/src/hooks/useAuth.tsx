import { createContext, useContext, useState, useEffect, useCallback, ReactNode } from 'react'
import { User, AuthResponse } from '@/types'
import { authApi } from '@/services/api'

interface AuthContextType {
  user: User | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (email: string, password: string) => Promise<void>
  register: (data: {
    email: string
    password: string
    firstName: string
    lastName?: string
    phone?: string
    role: 'CUSTOMER' | 'BUSINESS_OWNER'
  }) => Promise<void>
  logout: () => Promise<void>
  isOwner: boolean
  isCustomer: boolean
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  // Check for existing session on mount
  useEffect(() => {
    const checkAuth = async () => {
      const token = localStorage.getItem('accessToken')
      if (token) {
        try {
          const response = await authApi.me()
          setUser(response.data)
        } catch {
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
        }
      }
      setIsLoading(false)
    }
    checkAuth()
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login(email, password)
    const { accessToken, refreshToken, user } = response.data as AuthResponse
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    setUser(user)
  }, [])

  const register = useCallback(async (data: {
    email: string
    password: string
    firstName: string
    lastName?: string
    phone?: string
    role: 'CUSTOMER' | 'BUSINESS_OWNER'
  }) => {
    const response = await authApi.register(data)
    const { accessToken, refreshToken, user } = response.data as AuthResponse
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
    setUser(user)
  }, [])

  const logout = useCallback(async () => {
    try {
      await authApi.logout()
    } catch {
      // Ignore logout errors
    } finally {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      setUser(null)
    }
  }, [])

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    login,
    register,
    logout,
    isOwner: user?.role === 'BUSINESS_OWNER',
    isCustomer: user?.role === 'CUSTOMER',
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}
