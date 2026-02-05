import { useSearchParams } from 'react-router-dom'

export default function Search() {
  const [searchParams] = useSearchParams()
  const query = searchParams.get('q')
  const lat = searchParams.get('lat')
  const lng = searchParams.get('lng')

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 flex items-center gap-4">
          <a href="/" className="text-xl font-bold text-primary-600">Findly</a>
          <input
            type="text"
            defaultValue={query || ''}
            placeholder="Search services..."
            className="flex-1 px-4 py-2 rounded-lg border border-gray-300"
          />
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-2xl font-bold text-gray-900">
            {query ? `Results for "${query}"` : 'Nearby Services'}
          </h1>
          {lat && lng && (
            <span className="text-sm text-gray-500">
              Within 5km of your location
            </span>
          )}
        </div>

        {/* Placeholder for results */}
        <div className="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[1, 2, 3, 4, 5, 6].map((i) => (
            <div key={i} className="card p-4 animate-pulse">
              <div className="h-40 bg-gray-200 rounded mb-4"></div>
              <div className="h-4 bg-gray-200 rounded w-3/4 mb-2"></div>
              <div className="h-4 bg-gray-200 rounded w-1/2"></div>
            </div>
          ))}
        </div>

        <p className="text-center text-gray-500 mt-8">
          Connect backend to see real results
        </p>
      </main>
    </div>
  )
}
