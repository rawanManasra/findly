import { Routes, Route } from 'react-router-dom'
import { Suspense, lazy } from 'react'

// Lazy load pages
const Home = lazy(() => import('@/pages/consumer/Home'))
const Search = lazy(() => import('@/pages/consumer/Search'))
const BusinessDetail = lazy(() => import('@/pages/consumer/BusinessDetail'))
const Login = lazy(() => import('@/pages/auth/Login'))
const Register = lazy(() => import('@/pages/auth/Register'))
const OwnerDashboard = lazy(() => import('@/pages/owner/Dashboard'))

// Loading component
const PageLoader = () => (
  <div className="min-h-screen flex items-center justify-center">
    <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
  </div>
)

function App() {
  return (
    <Suspense fallback={<PageLoader />}>
      <Routes>
        {/* Consumer Routes */}
        <Route path="/" element={<Home />} />
        <Route path="/search" element={<Search />} />
        <Route path="/business/:id" element={<BusinessDetail />} />

        {/* Auth Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Owner Routes */}
        <Route path="/owner" element={<OwnerDashboard />} />
        <Route path="/owner/*" element={<OwnerDashboard />} />

        {/* 404 */}
        <Route path="*" element={<NotFound />} />
      </Routes>
    </Suspense>
  )
}

function NotFound() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center">
      <h1 className="text-4xl font-bold text-gray-900">404</h1>
      <p className="mt-2 text-gray-600">Page not found</p>
      <a href="/" className="mt-4 text-primary-600 hover:text-primary-700">
        Go back home
      </a>
    </div>
  )
}

export default App
