import { useStationContext } from '@/context/StationContext'

export function useActiveStation() {
  return useStationContext()
}
