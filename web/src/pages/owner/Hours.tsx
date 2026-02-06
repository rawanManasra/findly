import { useEffect, useState } from 'react'
import OwnerLayout from '@/components/owner/OwnerLayout'
import { ownerApi } from '@/services/api'
import { Business, WorkingHoursResponse } from '@/types'

const dayNames = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday']

interface HoursFormData {
  dayOfWeek: number
  startTime: string
  endTime: string
  closed: boolean
  breakStart: string
  breakEnd: string
}

export default function Hours() {
  const [businesses, setBusinesses] = useState<Business[]>([])
  const [selectedBusinessId, setSelectedBusinessId] = useState<string>('')
  const [hours, setHours] = useState<WorkingHoursResponse[]>([])
  const [formData, setFormData] = useState<HoursFormData[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    loadBusinesses()
  }, [])

  useEffect(() => {
    if (selectedBusinessId) {
      loadHours()
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

  const loadHours = async () => {
    try {
      setLoading(true)
      const response = await ownerApi.getWorkingHours(selectedBusinessId)
      const hoursData = response.data || []
      setHours(hoursData)

      // Initialize form data for all 7 days
      const initialFormData: HoursFormData[] = dayNames.map((_, index) => {
        const existing = hoursData.find((h: WorkingHoursResponse) => h.dayOfWeek === index)
        return {
          dayOfWeek: index,
          startTime: existing?.startTime || '09:00',
          endTime: existing?.endTime || '17:00',
          closed: existing?.closed ?? (index === 0 || index === 6), // Default closed on weekends
          breakStart: existing?.breakStart || '',
          breakEnd: existing?.breakEnd || '',
        }
      })
      setFormData(initialFormData)
    } catch (err) {
      console.error('Failed to load hours:', err)
      setError('Failed to load working hours')
    } finally {
      setLoading(false)
    }
  }

  const handleChange = (dayIndex: number, field: keyof HoursFormData, value: string | boolean) => {
    setFormData((prev) =>
      prev.map((day, index) => (index === dayIndex ? { ...day, [field]: value } : day))
    )
    setSuccess(false)
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedBusinessId) return

    try {
      setSaving(true)
      setError(null)

      await ownerApi.updateWorkingHours(selectedBusinessId, {
        hours: formData.map((day) => ({
          dayOfWeek: day.dayOfWeek,
          startTime: day.closed ? undefined : day.startTime,
          endTime: day.closed ? undefined : day.endTime,
          closed: day.closed,
          breakStart: day.breakStart || undefined,
          breakEnd: day.breakEnd || undefined,
        })),
      })

      setSuccess(true)
      setTimeout(() => setSuccess(false), 3000)
    } catch (err) {
      console.error('Failed to save hours:', err)
      setError('Failed to save working hours')
    } finally {
      setSaving(false)
    }
  }

  const copyToAll = (sourceIndex: number) => {
    const source = formData[sourceIndex]
    setFormData((prev) =>
      prev.map((day) => ({
        ...day,
        startTime: source.startTime,
        endTime: source.endTime,
        closed: source.closed,
        breakStart: source.breakStart,
        breakEnd: source.breakEnd,
      }))
    )
  }

  if (businesses.length === 0 && !loading) {
    return (
      <OwnerLayout>
        <div className="text-center py-12">
          <h2 className="text-xl font-semibold text-gray-900 mb-2">No Business Yet</h2>
          <p className="text-gray-500 mb-4">Create a business first to set working hours.</p>
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
        <h2 className="text-2xl font-bold text-gray-900">Working Hours</h2>
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

      {success && (
        <div className="bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-lg mb-6">
          Working hours saved successfully!
        </div>
      )}

      {loading ? (
        <div className="flex items-center justify-center h-64">
          <div className="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-primary-500"></div>
        </div>
      ) : (
        <form onSubmit={handleSubmit}>
          <div className="card overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Day
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Hours
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Break
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase">
                    Actions
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {formData.map((day, index) => (
                  <tr key={index} className={day.closed ? 'bg-gray-50' : ''}>
                    <td className="px-6 py-4 whitespace-nowrap font-medium text-gray-900">
                      {dayNames[index]}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <label className="flex items-center gap-2 cursor-pointer">
                        <input
                          type="checkbox"
                          checked={!day.closed}
                          onChange={(e) => handleChange(index, 'closed', !e.target.checked)}
                          className="rounded border-gray-300 text-primary-600 focus:ring-primary-500"
                        />
                        <span className={day.closed ? 'text-gray-500' : 'text-green-600 font-medium'}>
                          {day.closed ? 'Closed' : 'Open'}
                        </span>
                      </label>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {!day.closed && (
                        <div className="flex items-center gap-2">
                          <input
                            type="time"
                            value={day.startTime}
                            onChange={(e) => handleChange(index, 'startTime', e.target.value)}
                            className="input-field w-32"
                          />
                          <span className="text-gray-500">to</span>
                          <input
                            type="time"
                            value={day.endTime}
                            onChange={(e) => handleChange(index, 'endTime', e.target.value)}
                            className="input-field w-32"
                          />
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {!day.closed && (
                        <div className="flex items-center gap-2">
                          <input
                            type="time"
                            value={day.breakStart}
                            onChange={(e) => handleChange(index, 'breakStart', e.target.value)}
                            className="input-field w-32"
                            placeholder="Start"
                          />
                          <span className="text-gray-500">-</span>
                          <input
                            type="time"
                            value={day.breakEnd}
                            onChange={(e) => handleChange(index, 'breakEnd', e.target.value)}
                            className="input-field w-32"
                            placeholder="End"
                          />
                        </div>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <button
                        type="button"
                        onClick={() => copyToAll(index)}
                        className="text-sm text-primary-600 hover:text-primary-700"
                      >
                        Copy to all
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="mt-6 flex justify-end">
            <button type="submit" disabled={saving} className="btn-primary disabled:opacity-50">
              {saving ? 'Saving...' : 'Save Working Hours'}
            </button>
          </div>
        </form>
      )}
    </OwnerLayout>
  )
}
