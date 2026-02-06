import { useEffect, useState } from 'react'
import OwnerLayout from '@/components/owner/OwnerLayout'
import { ownerApi } from '@/services/api'
import { Business, ServiceResponse } from '@/types'

interface ServiceFormData {
  name: string
  description: string
  durationMins: number
  price: number
  currency: string
}

const defaultFormData: ServiceFormData = {
  name: '',
  description: '',
  durationMins: 30,
  price: 0,
  currency: 'USD',
}

export default function Services() {
  const [businesses, setBusinesses] = useState<Business[]>([])
  const [selectedBusinessId, setSelectedBusinessId] = useState<string>('')
  const [services, setServices] = useState<ServiceResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  // Modal state
  const [showModal, setShowModal] = useState(false)
  const [editingService, setEditingService] = useState<ServiceResponse | null>(null)
  const [formData, setFormData] = useState<ServiceFormData>(defaultFormData)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    loadBusinesses()
  }, [])

  useEffect(() => {
    if (selectedBusinessId) {
      loadServices()
    }
  }, [selectedBusinessId])

  const loadBusinesses = async () => {
    try {
      const response = await ownerApi.getMyBusinesses()
      const data = response.data.content || response.data || []
      setBusinesses(data)
      if (data.length > 0 && !selectedBusinessId) {
        setSelectedBusinessId(data[0].id)
      }
    } catch (err) {
      console.error('Failed to load businesses:', err)
      setError('Failed to load businesses')
    } finally {
      setLoading(false)
    }
  }

  const loadServices = async () => {
    try {
      setLoading(true)
      const response = await ownerApi.getServices(selectedBusinessId)
      setServices(response.data || [])
    } catch (err) {
      console.error('Failed to load services:', err)
      setError('Failed to load services')
    } finally {
      setLoading(false)
    }
  }

  const openAddModal = () => {
    setEditingService(null)
    setFormData(defaultFormData)
    setShowModal(true)
  }

  const openEditModal = (service: ServiceResponse) => {
    setEditingService(service)
    setFormData({
      name: service.name,
      description: service.description || '',
      durationMins: service.durationMins,
      price: service.price || 0,
      currency: service.currency,
    })
    setShowModal(true)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedBusinessId) return

    try {
      setSaving(true)
      if (editingService) {
        await ownerApi.updateService(selectedBusinessId, editingService.id, formData)
      } else {
        await ownerApi.addService(selectedBusinessId, formData)
      }
      setShowModal(false)
      loadServices()
    } catch (err) {
      console.error('Failed to save service:', err)
      setError('Failed to save service')
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (serviceId: string) => {
    if (!confirm('Are you sure you want to delete this service?')) return

    try {
      await ownerApi.deleteService(selectedBusinessId, serviceId)
      loadServices()
    } catch (err) {
      console.error('Failed to delete service:', err)
      setError('Failed to delete service')
    }
  }

  const toggleActive = async (service: ServiceResponse) => {
    try {
      await ownerApi.updateService(selectedBusinessId, service.id, {
        active: !service.active,
      })
      loadServices()
    } catch (err) {
      console.error('Failed to toggle service:', err)
    }
  }

  if (businesses.length === 0 && !loading) {
    return (
      <OwnerLayout>
        <div className="text-center py-12">
          <h2 className="text-xl font-semibold text-gray-900 mb-2">No Business Yet</h2>
          <p className="text-gray-500 mb-4">Create a business first to add services.</p>
          <a href="/owner/settings" className="btn-primary">
            Create Business
          </a>
        </div>
      </OwnerLayout>
    )
  }

  return (
    <OwnerLayout>
      <div className="flex justify-between items-center mb-8">
        <h2 className="text-2xl font-bold text-gray-900">Services</h2>
        <button onClick={openAddModal} className="btn-primary">
          + Add Service
        </button>
      </div>

      {/* Business Selector */}
      {businesses.length > 1 && (
        <div className="mb-6">
          <label className="block text-sm font-medium text-gray-700 mb-1">Business</label>
          <select
            value={selectedBusinessId}
            onChange={(e) => setSelectedBusinessId(e.target.value)}
            className="input-field w-64"
          >
            {businesses.map((business) => (
              <option key={business.id} value={business.id}>
                {business.name}
              </option>
            ))}
          </select>
        </div>
      )}

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6">
          {error}
        </div>
      )}

      {/* Services Grid */}
      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
        </div>
      ) : services.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {services.map((service) => (
            <div
              key={service.id}
              className={`card p-6 ${!service.active ? 'opacity-60' : ''}`}
            >
              <div className="flex justify-between items-start mb-4">
                <div>
                  <h3 className="font-semibold text-gray-900">{service.name}</h3>
                  {service.description && (
                    <p className="text-sm text-gray-500 mt-1">{service.description}</p>
                  )}
                </div>
                <button
                  onClick={() => toggleActive(service)}
                  className={`px-2 py-1 rounded text-xs font-medium ${
                    service.active
                      ? 'bg-green-100 text-green-800'
                      : 'bg-gray-100 text-gray-600'
                  }`}
                >
                  {service.active ? 'Active' : 'Inactive'}
                </button>
              </div>

              <div className="flex items-center gap-4 text-sm text-gray-600 mb-4">
                <span>{service.durationMins} min</span>
                <span>•</span>
                <span className="font-medium text-gray-900">{service.formattedPrice}</span>
              </div>

              <div className="flex gap-2">
                <button
                  onClick={() => openEditModal(service)}
                  className="flex-1 px-3 py-2 text-sm text-primary-600 border border-primary-200 rounded-lg hover:bg-primary-50"
                >
                  Edit
                </button>
                <button
                  onClick={() => handleDelete(service.id)}
                  className="px-3 py-2 text-sm text-red-600 border border-red-200 rounded-lg hover:bg-red-50"
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="card p-12 text-center">
          <p className="text-gray-500 mb-4">No services yet. Add your first service.</p>
          <button onClick={openAddModal} className="btn-primary">
            + Add Service
          </button>
        </div>
      )}

      {/* Add/Edit Modal */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-6 w-full max-w-md mx-4">
            <h3 className="text-lg font-semibold text-gray-900 mb-4">
              {editingService ? 'Edit Service' : 'Add Service'}
            </h3>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Service Name *
                </label>
                <input
                  type="text"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="input-field w-full"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Description
                </label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="input-field w-full"
                  rows={3}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Duration (min) *
                  </label>
                  <input
                    type="number"
                    value={formData.durationMins}
                    onChange={(e) =>
                      setFormData({ ...formData, durationMins: parseInt(e.target.value) || 0 })
                    }
                    className="input-field w-full"
                    min={5}
                    step={5}
                    required
                  />
                </div>

                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Price
                  </label>
                  <input
                    type="number"
                    value={formData.price}
                    onChange={(e) =>
                      setFormData({ ...formData, price: parseFloat(e.target.value) || 0 })
                    }
                    className="input-field w-full"
                    min={0}
                    step={0.01}
                  />
                </div>
              </div>

              <div className="flex gap-3 pt-4">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="flex-1 px-4 py-2 text-gray-600 border rounded-lg hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="flex-1 btn-primary disabled:opacity-50"
                >
                  {saving ? 'Saving...' : editingService ? 'Update' : 'Add Service'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </OwnerLayout>
  )
}
