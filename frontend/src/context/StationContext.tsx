import {
  createContext,
  useContext,
  useState,
  type ReactNode,
} from 'react'

export type ActiveStation = {
  id: string
  name: string
}

type StationContextValue = {
  station: ActiveStation | null
  setStation: (s: ActiveStation) => void
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

  // Detecta se o posto salvo ainda existe — se não, limpa
  // (tratado via error boundary em cada page que usa station)

  return (
    <StationContext.Provider value={{ station, setStation }}>
      {children}
    </StationContext.Provider>
  )
}

export function useStationContext(): StationContextValue {
  const ctx = useContext(StationContext)
  if (!ctx) throw new Error('useStationContext must be used within StationProvider')
  return ctx
}
