import { useState } from 'react'
import { useNavigate } from 'react-router-dom'

export default function Home() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState('')
  const [locationStatus, setLocationStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle')

  const handleGetLocation = () => {
    setLocationStatus('loading')
    if ('geolocation' in navigator) {
      navigator.geolocation.getCurrentPosition(
        (position) => {
          setLocationStatus('success')
          navigate(`/search?lat=${position.coords.latitude}&lng=${position.coords.longitude}`)
        },
        () => {
          setLocationStatus('error')
        }
      )
    } else {
      setLocationStatus('error')
    }
  }

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault()
    navigate(`/search?q=${encodeURIComponent(searchQuery)}`)
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-primary-50 to-primary-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        {/* Header */}
        <header className="flex justify-between items-center mb-16">
          <h1 className="text-2xl font-bold text-primary-600">Findly</h1>
          <nav className="flex gap-4">
            <a href="/login" className="text-gray-600 hover:text-gray-900">Login</a>
            <a href="/register" className="btn-primary">Sign Up</a>
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
            className="inline-flex items-center gap-2 text-primary-600 hover:text-primary-700 font-medium"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
            </svg>
            {locationStatus === 'loading' ? 'Getting location...' : 'Use my location'}
          </button>
          {locationStatus === 'error' && (
            <p className="mt-2 text-sm text-red-600">Could not get your location. Please enable location services.</p>
          )}
        </div>

        {/* Categories */}
        <div className="mt-16">
          <h3 className="text-xl font-semibold text-gray-900 mb-6 text-center">Popular Categories</h3>
          <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
            {['Hair Salon', 'Barber', 'Spa & Massage', 'Dentist', 'Personal Trainer', 'Cleaning', 'Plumber', 'Electrician'].map((category) => (
              <a
                key={category}
                href={`/search?category=${encodeURIComponent(category.toLowerCase())}`}
                className="card p-4 text-center hover:shadow-lg transition-shadow"
              >
                <span className="text-gray-900 font-medium">{category}</span>
              </a>
            ))}
          </div>
        </div>
      </div>
    </div>
  )
}
