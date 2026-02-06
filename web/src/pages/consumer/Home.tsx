import { useState, useEffect } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/hooks/useAuth'
import { categoryApi } from '@/services/api'
import { Category } from '@/types'

export default function Home() {
  const navigate = useNavigate()
  const { user, isAuthenticated, logout, isOwner } = useAuth()
  const [searchQuery, setSearchQuery] = useState('')
  const [locationStatus, setLocationStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle')
  const [locationError, setLocationError] = useState<string | null>(null)

  // Fetch categories
  const { data: categories } = useQuery({
    queryKey: ['categories'],
    queryFn: async () => {
      const response = await categoryApi.getAll()
      return response.data as Category[]
    },
  })

  const handleGetLocation = () => {
    setLocationStatus('loading')
    setLocationError(null)

    if (!('geolocation' in navigator)) {
      setLocationStatus('error')
      setLocationError('Geolocation is not supported by your browser')
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLocationStatus('success')
        navigate(`/search?lat=${position.coords.latitude}&lng=${position.coords.longitude}`)
      },
      (err) => {
        setLocationStatus('error')
        switch (err.code) {
          case err.PERMISSION_DENIED:
            setLocationError('Location permission denied. Please enable location access in your browser settings.')
            break
          case err.POSITION_UNAVAILABLE:
            setLocationError('Location information is unavailable.')
            break
          case err.TIMEOUT:
            setLocationError('Location request timed out. Please try again.')
            break
          default:
            setLocationError('An error occurred while getting your location.')
        }
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000,
      }
    )
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    if (searchQuery.trim()) {
      // Try to get location for search, fall back to search without location
      if ('geolocation' in navigator) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            navigate(`/search?q=${encodeURIComponent(searchQuery)}&lat=${position.coords.latitude}&lng=${position.coords.longitude}`)
          },
          () => {
            navigate(`/search?q=${encodeURIComponent(searchQuery)}`)
          },
          { timeout: 3000 }
        )
      } else {
        navigate(`/search?q=${encodeURIComponent(searchQuery)}`)
      }
    }
  }

  const handleCategoryClick = (category: Category) => {
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          navigate(`/search?category=${category.slug}&lat=${position.coords.latitude}&lng=${position.coords.longitude}`)
        },
        () => {
          navigate(`/search?category=${category.slug}`)
        },
        { timeout: 3000 }
      )
    } else {
      navigate(`/search?category=${category.slug}`)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Header */}
        <header className="flex justify-between items-center mb-16">
          <h1 className="text-2xl font-bold text-primary-600">Findly</h1>
          <nav className="flex items-center gap-4">
            {isAuthenticated ? (
              <>
                <span className="text-gray-600">Hi, {user?.firstName}</span>
                {isOwner && (
                  <Link to="/owner" className="text-primary-600 hover:text-primary-700">
                    Dashboard
                  </Link>
                )}
                <Link to="/my-bookings" className="text-gray-600 hover:text-gray-900">
                  My Bookings
                </Link>
                <button onClick={logout} className="text-gray-600 hover:text-gray-900">
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="text-gray-600 hover:text-gray-900">
                  Login
                </Link>
                <Link to="/register" className="btn-primary">
                  Sign Up
                </Link>
              </>
            )}
          </nav>
        </header>

        {/* Hero Section */}
        <div className="text-center max-w-3xl mx-auto">
          <h2 className="text-4xl sm:text-5xl font-bold text-gray-900 mb-6">
            Find & Book Local Services
          </h2>
          <p className="text-xl text-gray-600 mb-8">
            Discover businesses near you and book appointments instantly
          </p>

          {/* Search Form */}
          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-4 mb-8">
            <input
              type="text"
              placeholder="What service are you looking for?"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="flex-1 px-4 py-3 rounded-lg border border-gray-300 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
            />
            <button type="submit" className="btn-primary px-8 py-3">
              Search
            </button>
          </form>

          {/* Location Button */}
          <button
            onClick={handleGetLocation}
            disabled={locationStatus === 'loading'}
            className="inline-flex items-center gap-2 text-primary-600 hover:text-primary-700 font-medium disabled:opacity-50"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            {locationStatus === 'loading' ? (
              <>
                <span className="animate-spin h-4 w-4 border-2 border-primary-600 border-t-transparent rounded-full"></span>
                Getting location...
              </>
            ) : (
              'Use my location'
            )}
          </button>
          {locationStatus === 'error' && locationError && (
            <p className="mt-2 text-sm text-red-600">{locationError}</p>
          )}
        </div>

        {/* Categories */}
        <div className="mt-16">
          <h3 className="text-xl font-semibold text-gray-900 mb-6 text-center">Popular Categories</h3>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {categories && categories.length > 0 ? (
              categories.slice(0, 8).map((category) => (
                <button
                  key={category.id}
                  onClick={() => handleCategoryClick(category)}
                  className="card p-4 text-center hover:shadow-lg transition-shadow cursor-pointer"
                >
                  {category.icon && <span className="text-2xl mb-2 block">{category.icon}</span>}
                  <span className="text-gray-900 font-medium">{category.name}</span>
                </button>
              ))
            ) : (
              // Fallback categories if API not available
              ['Hair Salon', 'Barber', 'Spa & Massage', 'Dentist', 'Personal Trainer', 'Cleaning', 'Plumber', 'Electrician'].map((name) => (
                <button
                  key={name}
                  onClick={() => navigate(`/search?q=${encodeURIComponent(name.toLowerCase())}`)}
                  className="card p-4 text-center hover:shadow-lg transition-shadow cursor-pointer"
                >
                  <span className="text-gray-900 font-medium">{name}</span>
                </button>
              ))
            )}
          </div>
        </div>

        {/* How it works */}
        <div className="mt-20">
          <h3 className="text-xl font-semibold text-gray-900 mb-8 text-center">How it Works</h3>
          <div className="grid md:grid-cols-3 gap-8">
            <div className="text-center">
              <div className="w-12 h-12 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Search</h4>
              <p className="text-gray-600 text-sm">Find services near you by category or keyword</p>
            </div>
            <div className="text-center">
              <div className="w-12 h-12 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Book</h4>
              <p className="text-gray-600 text-sm">Choose a time slot and book your appointment</p>
            </div>
            <div className="text-center">
              <div className="w-12 h-12 bg-primary-100 text-primary-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h4 className="font-semibold text-gray-900 mb-2">Confirm</h4>
              <p className="text-gray-600 text-sm">Get confirmation and show up at your appointment</p>
            </div>
          </div>
        </div>

        {/* CTA for business owners */}
        {!isAuthenticated && (
          <div className="mt-20 text-center bg-white rounded-2xl p-8 shadow-sm">
            <h3 className="text-xl font-semibold text-gray-900 mb-2">Own a business?</h3>
            <p className="text-gray-600 mb-4">List your services and reach more customers</p>
            <Link to="/register?role=business" className="btn-primary">
              Register Your Business
            </Link>
          </div>
        )}
      </div>
    </div>
  )
}
