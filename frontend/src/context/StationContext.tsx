import {
  createContext,
  useContext,
  useEffect,
  useState,
  type ReactNode,
} from 'react'
import { getStation } from '@/api/stations'

export type ActiveStation = {
  id: string
  name: string
}

type StationContextValue = {
  station: ActiveStation | null
  setStation: (s: ActiveStation) => void
  clearStation: () => void
}

const StationContext = createContext<StationContextValue | null>(null)

const STORAGE_KEY = 'octane-station'

export function StationProvider({ children }: { children: ReactNode }) {
  const [station, setStationState] = useState<ActiveStation | null>(() => {
    try {
      const saved = localStorage.getItem(STORAGE_KEY)
      return saved ? (JSON.parse(saved) as ActiveStation) : null
    } catch {
      return null
    }
  })

  function setStation(s: ActiveStation) {
    setStationState(s)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(s))
  }

  function clearStation() {
    setStationState(null)
    localStorage.removeItem(STORAGE_KEY)
  }

  useEffect(() => {
    if (!station) return
    getStation(station.id)
      .then((s) => {
        if (s.active === false) clearStation()
      })
      .catch(() => {
        // Backend offline or network error — keep station as-is
      })
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  return (
    <StationContext.Provider value={{ station, setStation, clearStation }}>
      {children}
    </StationContext.Provider>
  )
}

export function useStationContext(): StationContextValue {
  const ctx = useContext(StationContext)
  if (!ctx) throw new Error('useStationContext must be used within StationProvider')
  return ctx
}
