import { useQuery } from '@tanstack/react-query'
import { useActiveStation } from './useActiveStation'
import { getOpenShift } from '@/api/shifts'

export function useShift() {
  const { station } = useActiveStation()

  return useQuery({
    queryKey: ['shift', 'open', station?.id],
    queryFn: () => getOpenShift(station!.id),
    enabled: !!station,
  })
}
