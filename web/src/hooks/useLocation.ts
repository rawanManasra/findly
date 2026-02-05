import { useState, useCallback } from 'react'

interface Location {
  latitude: number
  longitude: number
}

interface UseLocationReturn {
  location: Location | null
  error: string | null
  loading: boolean
  getLocation: () => void
}

export function useLocation(): UseLocationReturn {
  const [location, setLocation] = useState<Location | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  const getLocation = useCallback(() => {
    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser')
      return
    }

    setLoading(true)
    setError(null)

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLocation({
          latitude: position.coords.latitude,
          longitude: position.coords.longitude,
        })
        setLoading(false)
      },
      (err) => {
        let errorMessage = 'Failed to get location'
        switch (err.code) {
          case err.PERMISSION_DENIED:
            errorMessage = 'Location permission denied. Please enable location access.'
            break
          case err.POSITION_UNAVAILABLE:
            errorMessage = 'Location information unavailable'
            break
          case err.TIMEOUT:
            errorMessage = 'Location request timed out'
            break
        }
        setError(errorMessage)
        setLoading(false)
      },
      {
        enableHighAccuracy: true,
        timeout: 10000,
        maximumAge: 300000, // 5 minutes
      }
    )
  }, [])

  return { location, error, loading, getLocation }
}
