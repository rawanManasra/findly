import { Link, useLocation } from 'react-router-dom'
import { useAuth } from '@/hooks/useAuth'

interface OwnerLayoutProps {
  children: React.ReactNode
}

const navItems = [
  { path: '/owner', label: 'Dashboard', icon: '📊' },
  { path: '/owner/bookings', label: 'Bookings', icon: '📅' },
  { path: '/owner/services', label: 'Services', icon: '🛠' },
  { path: '/owner/hours', label: 'Working Hours', icon: '🕐' },
  { path: '/owner/settings', label: 'Settings', icon: '⚙️' },
]

export default function OwnerLayout({ children }: OwnerLayoutProps) {
  const location = useLocation()
  const { user, logout } = useAuth()

  const isActive = (path: string) => {
    if (path === '/owner') {
      return location.pathname === '/owner'
    }
    return location.pathname.startsWith(path)
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="flex">
        {/* Sidebar */}
        <aside className="w-64 bg-white shadow-sm min-h-screen p-4 fixed">
          <Link to="/owner" className="block">
            <h1 className="text-xl font-bold text-primary-600 mb-2">Findly Business</h1>
          </Link>
          {user && (
            <p className="text-sm text-gray-500 mb-8 truncate">{user.email}</p>
          )}

          <nav className="space-y-2">
            {navItems.map((item) => (
              <Link
                key={item.path}
                to={item.path}
                className={`flex items-center gap-3 px-4 py-2 rounded-lg transition-colors ${
                  isActive(item.path)
                    ? 'bg-primary-50 text-primary-700 font-medium'
                    : 'text-gray-600 hover:bg-gray-50'
                }`}
              >
                <span>{item.icon}</span>
                <span>{item.label}</span>
              </Link>
            ))}
          </nav>

          <div className="absolute bottom-4 left-4 right-4">
            <button
              onClick={logout}
              className="w-full px-4 py-2 text-left text-gray-600 hover:bg-gray-50 rounded-lg transition-colors"
            >
              Logout
            </button>
          </div>
        </aside>

        {/* Main Content */}
        <main className="flex-1 ml-64 p-8">
          {children}
        </main>
      </div>
    </div>
  )
}
