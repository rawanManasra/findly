export default function Dashboard() {
  return (
    <div className="min-h-screen bg-gray-50">
      {/* Sidebar */}
      <div className="flex">
        <aside className="w-64 bg-white shadow-sm min-h-screen p-4">
          <h1 className="text-xl font-bold text-primary-600 mb-8">Findly Business</h1>
          <nav className="space-y-2">
            <a href="/owner" className="block px-4 py-2 rounded-lg bg-primary-50 text-primary-700 font-medium">
              Dashboard
            </a>
            <a href="/owner/bookings" className="block px-4 py-2 rounded-lg text-gray-600 hover:bg-gray-50">
              Bookings
            </a>
            <a href="/owner/services" className="block px-4 py-2 rounded-lg text-gray-600 hover:bg-gray-50">
              Services
            </a>
            <a href="/owner/hours" className="block px-4 py-2 rounded-lg text-gray-600 hover:bg-gray-50">
              Working Hours
            </a>
            <a href="/owner/settings" className="block px-4 py-2 rounded-lg text-gray-600 hover:bg-gray-50">
              Settings
            </a>
          </nav>
        </aside>

        {/* Main Content */}
        <main className="flex-1 p-8">
          <h2 className="text-2xl font-bold text-gray-900 mb-8">Dashboard</h2>

          {/* Stats */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
            <div className="card p-6">
              <p className="text-sm text-gray-500">Today's Bookings</p>
              <p className="text-3xl font-bold text-gray-900">0</p>
            </div>
            <div className="card p-6">
              <p className="text-sm text-gray-500">Pending Approval</p>
              <p className="text-3xl font-bold text-yellow-600">0</p>
            </div>
            <div className="card p-6">
              <p className="text-sm text-gray-500">This Week</p>
              <p className="text-3xl font-bold text-gray-900">0</p>
            </div>
            <div className="card p-6">
              <p className="text-sm text-gray-500">Total Customers</p>
              <p className="text-3xl font-bold text-gray-900">0</p>
            </div>
          </div>

          {/* Recent Bookings */}
          <div className="card">
            <div className="px-6 py-4 border-b">
              <h3 className="text-lg font-semibold text-gray-900">Recent Bookings</h3>
            </div>
            <div className="p-6">
              <p className="text-gray-500 text-center py-8">
                No bookings yet. Connect backend to see real data.
              </p>
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}
