import { useEffect, useState } from 'react'
import OwnerLayout from '@/components/owner/OwnerLayout'
import { ownerApi, categoryApi } from '@/services/api'
import { Business, Category } from '@/types'

interface BusinessFormData {
  name: string
  description: string
  categoryId: string
  phone: string
  email: string
  website: string
}

interface LocationFormData {
  addressLine1: string
  addressLine2: string
  city: string
  state: string
  postalCode: string
  country: string
  latitude: number
  longitude: number
}

const defaultBusinessForm: BusinessFormData = {
  name: '',
  description: '',
  categoryId: '',
  phone: '',
  email: '',
  website: '',
}

const defaultLocationForm: LocationFormData = {
  addressLine1: '',
  addressLine2: '',
  city: '',
  state: '',
  postalCode: '',
  country: 'US',
  latitude: 0,
  longitude: 0,
}

export default function Settings() {
  const [businesses, setBusinesses] = useState<Business[]>([])
  const [categories, setCategories] = useState<Category[]>([])
  const [selectedBusiness, setSelectedBusiness] = useState<Business | null>(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState<string | null>(null)

  const [businessForm, setBusinessForm] = useState<BusinessFormData>(defaultBusinessForm)
  const [locationForm, setLocationForm] = useState<LocationFormData>(defaultLocationForm)
  const [activeTab, setActiveTab] = useState<'info' | 'location'>('info')

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const [businessesRes, categoriesRes] = await Promise.all([
        ownerApi.getMyBusinesses(),
        categoryApi.getAll(),
      ])

      const businessData = businessesRes.data.content || businessesRes.data || []
      setBusinesses(businessData)
      setCategories(categoriesRes.data || [])

      if (businessData.length > 0) {
        selectBusiness(businessData[0])
      }
    } catch (err) {
      console.error('Failed to load data:', err)
      setError('Failed to load business data')
    } finally {
      setLoading(false)
    }
  }

  const selectBusiness = (business: Business) => {
    setSelectedBusiness(business)
    setBusinessForm({
      name: business.name,
      description: business.description || '',
      categoryId: business.categoryId || '',
      phone: business.phone || '',
      email: business.email || '',
      website: business.website || '',
    })
    setLocationForm({
      addressLine1: business.addressLine1 || '',
      addressLine2: business.addressLine2 || '',
      city: business.city || '',
      state: business.state || '',
      postalCode: business.postalCode || '',
      country: business.country || 'US',
      latitude: business.latitude || 0,
      longitude: business.longitude || 0,
    })
  }

  const handleCreateBusiness = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      setSaving(true)
      setError(null)
      await ownerApi.createBusiness(businessForm)
      setSuccess('Business created successfully!')
      loadData()
    } catch (err) {
      console.error('Failed to create business:', err)
      setError('Failed to create business')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateBusiness = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedBusiness) return

    try {
      setSaving(true)
      setError(null)
      await ownerApi.updateBusiness(selectedBusiness.id, businessForm)
      setSuccess('Business updated successfully!')
      loadData()
    } catch (err) {
      console.error('Failed to update business:', err)
      setError('Failed to update business')
    } finally {
      setSaving(false)
    }
  }

  const handleUpdateLocation = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedBusiness) return

    try {
      setSaving(true)
      setError(null)
      await ownerApi.updateLocation(selectedBusiness.id, locationForm)
      setSuccess('Location updated successfully!')
      loadData()
    } catch (err) {
      console.error('Failed to update location:', err)
      setError('Failed to update location')
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteBusiness = async () => {
    if (!selectedBusiness) return
    if (!confirm('Are you sure you want to delete this business? This action cannot be undone.'))
      return

    try {
      await ownerApi.deleteBusiness(selectedBusiness.id)
      setSuccess('Business deleted')
      setSelectedBusiness(null)
      setBusinessForm(defaultBusinessForm)
      loadData()
    } catch (err) {
      console.error('Failed to delete business:', err)
      setError('Failed to delete business')
    }
  }

  const getCurrentLocation = () => {
    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser')
      return
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLocationForm((prev) => ({
          ...prev,
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        }))
      },
      (err) => {
        console.error('Geolocation error:', err)
        setError('Failed to get current location')
      }
    )
  }

  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(null), 3000)
      return () => clearTimeout(timer)
    }
  }, [success])

  if (loading) {
    return (
      <OwnerLayout>
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
        </div>
      </OwnerLayout>
    )
  }

  return (
    <OwnerLayout>
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-2xl font-bold text-gray-900">Settings</h2>
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {success && (
        <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-6">
          {success}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
        {/* Business List */}
        <div className="lg:col-span-1">
          <div className="card p-4">
            <h3 className="font-semibold text-gray-900 mb-4">Your Businesses</h3>
            <div className="space-y-2">
              {businesses.map((business) => (
                <button
                  key={business.id}
                  onClick={() => selectBusiness(business)}
                  className={`w-full text-left px-4 py-2 rounded-lg transition-colors ${
                    selectedBusiness?.id === business.id
                      ? 'bg-primary-50 text-primary-700 font-medium'
                      : 'hover:bg-gray-50 text-gray-700'
                  }`}
                >
                  {business.name}
                </button>
              ))}
              <button
                onClick={() => {
                  setSelectedBusiness(null)
                  setBusinessForm(defaultBusinessForm)
                  setLocationForm(defaultLocationForm)
                }}
                className={`w-full text-left px-4 py-2 rounded-lg transition-colors text-primary-600 hover:bg-primary-50 ${
                  !selectedBusiness ? 'bg-primary-50 font-medium' : ''
                }`}
              >
                + Add New Business
              </button>
            </div>
          </div>
        </div>

        {/* Business Form */}
        <div className="lg:col-span-3">
          <div className="card">
            {/* Tabs */}
            <div className="border-b">
              <div className="flex">
                <button
                  onClick={() => setActiveTab('info')}
                  className={`px-6 py-3 font-medium ${
                    activeTab === 'info'
                      ? 'border-b-2 border-primary-600 text-primary-600'
                      : 'text-gray-500 hover:text-gray-700'
                  }`}
                >
                  Business Info
                </button>
                {selectedBusiness && (
                  <button
                    onClick={() => setActiveTab('location')}
                    className={`px-6 py-3 font-medium ${
                      activeTab === 'location'
                        ? 'border-b-2 border-primary-600 text-primary-600'
                        : 'text-gray-500 hover:text-gray-700'
                    }`}
                  >
                    Location
                  </button>
                )}
              </div>
            </div>

            <div className="p-6">
              {activeTab === 'info' && (
                <form onSubmit={selectedBusiness ? handleUpdateBusiness : handleCreateBusiness}>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Business Name *
                      </label>
                      <input
                        type="text"
                        value={businessForm.name}
                        onChange={(e) =>
                          setBusinessForm({ ...businessForm, name: e.target.value })
                        }
                        className="input-field w-full"
                        required
                      />
                    </div>

                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Description
                      </label>
                      <textarea
                        value={businessForm.description}
                        onChange={(e) =>
                          setBusinessForm({ ...businessForm, description: e.target.value })
                        }
                        className="input-field w-full"
                        rows={3}
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Category
                      </label>
                      <select
                        value={businessForm.categoryId}
                        onChange={(e) =>
                          setBusinessForm({ ...businessForm, categoryId: e.target.value })
                        }
                        className="input-field w-full"
                      >
                        <option value="">Select a category</option>
                        {categories.map((cat) => (
                          <option key={cat.id} value={cat.id}>
                            {cat.name}
                          </option>
                        ))}
                      </select>
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Phone
                      </label>
                      <input
                        type="tel"
                        value={businessForm.phone}
                        onChange={(e) =>
                          setBusinessForm({ ...businessForm, phone: e.target.value })
                        }
                        className="input-field w-full"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Email
                      </label>
                      <input
                        type="email"
                        value={businessForm.email}
                        onChange={(e) =>
                          setBusinessForm({ ...businessForm, email: e.target.value })
                        }
                        className="input-field w-full"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Website
                      </label>
                      <input
                        type="url"
                        value={businessForm.website}
                        onChange={(e) =>
                          setBusinessForm({ ...businessForm, website: e.target.value })
                        }
                        className="input-field w-full"
                        placeholder="https://"
                      />
                    </div>
                  </div>

                  <div className="mt-6 flex justify-between">
                    {selectedBusiness && (
                      <button
                        type="button"
                        onClick={handleDeleteBusiness}
                        className="px-4 py-2 text-red-600 hover:bg-red-50 rounded-lg"
                      >
                        Delete Business
                      </button>
                    )}
                    <div className="ml-auto">
                      <button
                        type="submit"
                        disabled={saving}
                        className="btn-primary disabled:opacity-50"
                      >
                        {saving
                          ? 'Saving...'
                          : selectedBusiness
                          ? 'Update Business'
                          : 'Create Business'}
                      </button>
                    </div>
                  </div>
                </form>
              )}

              {activeTab === 'location' && selectedBusiness && (
                <form onSubmit={handleUpdateLocation}>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Address Line 1 *
                      </label>
                      <input
                        type="text"
                        value={locationForm.addressLine1}
                        onChange={(e) =>
                          setLocationForm({ ...locationForm, addressLine1: e.target.value })
                        }
                        className="input-field w-full"
                        required
                      />
                    </div>

                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Address Line 2
                      </label>
                      <input
                        type="text"
                        value={locationForm.addressLine2}
                        onChange={(e) =>
                          setLocationForm({ ...locationForm, addressLine2: e.target.value })
                        }
                        className="input-field w-full"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        City *
                      </label>
                      <input
                        type="text"
                        value={locationForm.city}
                        onChange={(e) =>
                          setLocationForm({ ...locationForm, city: e.target.value })
                        }
                        className="input-field w-full"
                        required
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        State / Province
                      </label>
                      <input
                        type="text"
                        value={locationForm.state}
                        onChange={(e) =>
                          setLocationForm({ ...locationForm, state: e.target.value })
                        }
                        className="input-field w-full"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Postal Code
                      </label>
                      <input
                        type="text"
                        value={locationForm.postalCode}
                        onChange={(e) =>
                          setLocationForm({ ...locationForm, postalCode: e.target.value })
                        }
                        className="input-field w-full"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Country *
                      </label>
                      <input
                        type="text"
                        value={locationForm.country}
                        onChange={(e) =>
                          setLocationForm({ ...locationForm, country: e.target.value })
                        }
                        className="input-field w-full"
                        required
                      />
                    </div>

                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Coordinates
                      </label>
                      <div className="flex items-center gap-4">
                        <input
                          type="number"
                          value={locationForm.latitude}
                          onChange={(e) =>
                            setLocationForm({
                              ...locationForm,
                              latitude: parseFloat(e.target.value) || 0,
                            })
                          }
                          className="input-field w-40"
                          placeholder="Latitude"
                          step="any"
                        />
                        <input
                          type="number"
                          value={locationForm.longitude}
                          onChange={(e) =>
                            setLocationForm({
                              ...locationForm,
                              longitude: parseFloat(e.target.value) || 0,
                            })
                          }
                          className="input-field w-40"
                          placeholder="Longitude"
                          step="any"
                        />
                        <button
                          type="button"
                          onClick={getCurrentLocation}
                          className="px-4 py-2 text-primary-600 border border-primary-200 rounded-lg hover:bg-primary-50"
                        >
                          Use Current Location
                        </button>
                      </div>
                      <p className="text-sm text-gray-500 mt-1">
                        These coordinates are used for location-based search
                      </p>
                    </div>
                  </div>

                  <div className="mt-6 flex justify-end">
                    <button
                      type="submit"
                      disabled={saving}
                      className="btn-primary disabled:opacity-50"
                    >
                      {saving ? 'Saving...' : 'Update Location'}
                    </button>
                  </div>
                </form>
              )}
            </div>
          </div>
        </div>
      </div>
    </OwnerLayout>
  )
}
