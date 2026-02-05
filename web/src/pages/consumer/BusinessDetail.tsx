import { useParams } from 'react-router-dom'

export default function BusinessDetail() {
  const { id } = useParams()

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4">
          <a href="/" className="text-xl font-bold text-primary-600">Findly</a>
        </div>
      </header>

      <main className="max-w-4xl mx-auto px-4 py-8">
        <div className="card">
          {/* Business Header */}
          <div className="h-48 bg-gray-200"></div>
          <div className="p-6">
            <h1 className="text-2xl font-bold text-gray-900 mb-2">Business Name</h1>
            <p className="text-gray-500 mb-4">Category • 2.5 km away</p>
            <p className="text-gray-600 mb-6">
              Business description placeholder. This will show the actual business details when connected to the backend.
            </p>

            {/* Services */}
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Services</h2>
            <div className="space-y-3 mb-6">
              {[1, 2, 3].map((i) => (
                <div key={i} className="flex justify-between items-center p-4 bg-gray-50 rounded-lg">
                  <div>
                    <h3 className="font-medium text-gray-900">Service {i}</h3>
                    <p className="text-sm text-gray-500">30 mins</p>
                  </div>
                  <div className="text-right">
                    <p className="font-medium text-gray-900">₪100</p>
                    <button className="text-sm text-primary-600 hover:text-primary-700">Book</button>
                  </div>
                </div>
              ))}
            </div>

            {/* Working Hours */}
            <h2 className="text-lg font-semibold text-gray-900 mb-4">Working Hours</h2>
            <div className="space-y-2 text-sm">
              {['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'].map((day) => (
                <div key={day} className="flex justify-between">
                  <span className="text-gray-600">{day}</span>
                  <span className="text-gray-900">9:00 - 18:00</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        <p className="text-center text-gray-500 mt-8">
          Business ID: {id} (Connect backend for real data)
        </p>
      </main>
    </div>
  )
}
