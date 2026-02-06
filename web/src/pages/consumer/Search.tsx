import { useState, useEffect } from 'react'
import { useSearchParams, Link, useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuth } from '@/hooks/useAuth'
import { businessApi } from '@/services/api'
import { Business } from '@/types'

function formatDistance(meters: number): string {
  if (meters < 1000) {
    return `${Math.round(meters)}m`
  }
  return `${(meters / 1000).toFixed(1)}km`
}

function BusinessCard({ business }: { business: Business }) {
  return (
    <Link to={`/business/${business.id}`} className="card hover:shadow-lg transition-shadow">
      {business.imageUrl ? (
        <img
          src={business.imageUrl}
          alt={business.name}
          className="h-40 w-full object-cover rounded-t-lg"
        />
      ) : (
        <div className="h-40 bg-gradient-to-br from-primary-100 to-primary-200 rounded-t-lg flex items-center justify-center">
          <span className="text-4xl text-primary-400">
            {business.name.charAt(0).toUpperCase()}
          </span>
        </div>
      )}
      <div className="p-4">
        <div className="flex justify-between items-start mb-2">
          <h3 className="font-semibold text-gray-900 line-clamp-1">{business.name}</h3>
          {business.isVerified && (
            <span className="text-primary-600" title="Verified">
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M6.267 3.455a3.066 3.066 0 001.745-.723 3.066 3.066 0 013.976 0 3.066 3.066 0 001.745.723 3.066 3.066 0 012.812 2.812c.051.643.304 1.254.723 1.745a3.066 3.066 0 010 3.976 3.066 3.066 0 00-.723 1.745 3.066 3.066 0 01-2.812 2.812 3.066 3.066 0 00-1.745.723 3.066 3.066 0 01-3.976 0 3.066 3.066 0 00-1.745-.723 3.066 3.066 0 01-2.812-2.812 3.066 3.066 0 00-.723-1.745 3.066 3.066 0 010-3.976 3.066 3.066 0 00.723-1.745 3.066 3.066 0 012.812-2.812zm7.44 5.252a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
            </span>
          )}
        </div>
        <p className="text-sm text-gray-500 mb-2 line-clamp-2">
          {business.description || 'No description available'}
        </p>
        <div className="flex items-center justify-between text-sm">
          <div className="flex items-center gap-2">
            {business.ratingCount > 0 && (
              <span className="flex items-center gap-1">
                <svg className="w-4 h-4 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                  <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                </svg>
                <span className="text-gray-600">
                  {business.ratingAvg.toFixed(1)} ({business.ratingCount})
                </span>
              </span>
            )}
          </div>
          {business.distance !== undefined && (
            <span className="text-gray-500 flex items-center gap-1">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              {formatDistance(business.distance)}
            </span>
          )}
        </div>
        {business.city && (
          <p className="text-xs text-gray-400 mt-2">{business.city}</p>
        )}
      </div>
    </Link>
  )
}

export default function Search() {
  const navigate = useNavigate()
  const { user, isAuthenticated, logout, isOwner } = useAuth()
  const [searchParams, setSearchParams] = useSearchParams()

  const query = searchParams.get('q') || ''
  const lat = searchParams.get('lat')
  const lng = searchParams.get('lng')
  const category = searchParams.get('category')

  const [searchInput, setSearchInput] = useState(query)
  const [noLocation, setNoLocation] = useState(false)

  // Request location if not provided
  useEffect(() => {
    if (!lat || !lng) {
      if ('geolocation' in navigator) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const newParams = new URLSearchParams(searchParams)
            newParams.set('lat', position.coords.latitude.toString())
            newParams.set('lng', position.coords.longitude.toString())
            setSearchParams(newParams)
          },
          () => {
            setNoLocation(true)
          },
          { timeout: 5000 }
        )
      } else {
        setNoLocation(true)
      }
    }
  }, [])

  // Fetch businesses
  const { data: businesses, isLoading, error } = useQuery({
    queryKey: ['businesses', lat, lng, query, category],
    queryFn: async () => {
      if (!lat || !lng) return []
      const response = await businessApi.search({
        lat: parseFloat(lat),
        lng: parseFloat(lng),
        radius: 5000,
        q: query || undefined,
        category: category || undefined,
      })
      return response.data.content as Business[]
    },
    enabled: !!lat && !!lng,
  })

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    const newParams = new URLSearchParams(searchParams)
    if (searchInput.trim()) {
      newParams.set('q', searchInput.trim())
    } else {
      newParams.delete('q')
    }
    newParams.delete('category')
    setSearchParams(newParams)
  }

  const clearFilters = () => {
    const newParams = new URLSearchParams()
    if (lat) newParams.set('lat', lat)
    if (lng) newParams.set('lng', lng)
    setSearchParams(newParams)
    setSearchInput('')
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center gap-4">
            <Link to="/" className="text-xl font-bold text-primary-600 shrink-0">
              Findly
            </Link>
            <form onSubmit={handleSearch} className="flex-1 flex gap-2">
              <input
                type="text"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                placeholder="Search services..."
                className="flex-1 px-4 py-2 rounded-lg border border-gray-300 focus:ring-2 focus:ring-primary-500 focus:border-transparent"
              />
              <button type="submit" className="btn-primary px-4 py-2">
                Search
              </button>
            </form>
            <nav className="hidden md:flex items-center gap-4">
              {isAuthenticated ? (
                <>
                  {isOwner && (
                    <Link to="/owner" className="text-primary-600 hover:text-primary-700">
                      Dashboard
                    </Link>
                  )}
                  <Link to="/my-bookings" className="text-gray-600 hover:text-gray-900">
                    Bookings
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
                  <Link to="/register" className="btn-primary px-4 py-2">
                    Sign Up
                  </Link>
                </>
              )}
            </nav>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        {/* Location warning */}
        {noLocation && (
          <div className="mb-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
            <div className="flex items-start gap-3">
              <svg className="w-5 h-5 text-yellow-600 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
              <div>
                <p className="font-medium text-yellow-800">Location required</p>
                <p className="text-sm text-yellow-700">
                  Please enable location access to find businesses near you.
                </p>
                <button
                  onClick={() => window.location.reload()}
                  className="mt-2 text-sm text-yellow-800 underline hover:no-underline"
                >
                  Try again
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Results header */}
        <div className="flex justify-between items-center mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">
              {query ? `Results for "${query}"` : category ? `${category} services` : 'Nearby Services'}
            </h1>
            {lat && lng && (
              <p className="text-sm text-gray-500 mt-1">
                Within 5km of your location
              </p>
            )}
          </div>
          {(query || category) && (
            <button
              onClick={clearFilters}
              className="text-sm text-primary-600 hover:text-primary-700"
            >
              Clear filters
            </button>
          )}
        </div>

        {/* Loading state */}
        {isLoading && (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3, 4, 5, 6].map((i) => (
              <div key={i} className="card animate-pulse">
                <div className="h-40 bg-gray-200 rounded-t-lg"></div>
                <div className="p-4">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
                  <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Error state */}
        {error && (
          <div className="text-center py-12">
            <svg className="w-12 h-12 text-red-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p className="text-gray-600">Failed to load businesses. Please try again.</p>
          </div>
        )}

        {/* Empty state */}
        {!isLoading && !error && businesses && businesses.length === 0 && (
          <div className="text-center py-12">
            <svg className="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <p className="text-gray-600 mb-4">No businesses found in your area.</p>
            {(query || category) && (
              <button onClick={clearFilters} className="btn-primary">
                Show all nearby
              </button>
            )}
          </div>
        )}

        {/* Results grid */}
        {!isLoading && !error && businesses && businesses.length > 0 && (
          <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
            {businesses.map((business) => (
              <BusinessCard key={business.id} business={business} />
            ))}
          </div>
        )}
      </main>
    </div>
  )
}
