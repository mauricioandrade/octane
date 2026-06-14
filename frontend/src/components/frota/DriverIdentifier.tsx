import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { identifyFleetDriver } from '@/api/fleet-drivers'
import { ApiError } from '@/lib/api-client'
import type { FleetDriverIdentification } from '@/types'

type IdentifierType = 'CPF' | 'PIN' | 'RFID'

type Props = {
  stationId: string
  onIdentified: (result: FleetDriverIdentification) => void
}

export function DriverIdentifier({ stationId, onIdentified }: Props) {
  const [identifierType, setIdentifierType] = useState<IdentifierType>('CPF')
  const [cpf, setCpf] = useState('')
  const [pin, setPin] = useState('')
  const [rfidTag, setRfidTag] = useState('')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)
  const [identified, setIdentified] = useState<FleetDriverIdentification | null>(null)

  const mutation = useMutation({
    mutationFn: () =>
      identifyFleetDriver({
        stationId,
        cpf: identifierType !== 'RFID' ? cpf : '',
        pin: identifierType === 'PIN' ? pin : undefined,
        rfidTag: identifierType === 'RFID' ? rfidTag : undefined,
        identifierType,
      }),
    onSuccess: (result) => {
      setErrorMsg(null)
      setIdentified(result)
      onIdentified(result)
    },
    onError: (err) => {
      setIdentified(null)
      if (err instanceof ApiError && err.status === 404) {
        setErrorMsg('Motorista não encontrado')
      } else {
        setErrorMsg('Erro ao identificar motorista')
      }
    },
  })

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setErrorMsg(null)
    setIdentified(null)
    mutation.mutate()
  }

  return (
    <form onSubmit={handleSubmit} className="flex flex-col gap-3">
      <div>
        <Label>Tipo de identificação</Label>
        <Select
          value={identifierType}
          onValueChange={(v) => {
            setIdentifierType(v as IdentifierType)
            setErrorMsg(null)
            setIdentified(null)
          }}
        >
          <SelectTrigger>
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="CPF">CPF</SelectItem>
            <SelectItem value="PIN">CPF + PIN</SelectItem>
            <SelectItem value="RFID">Tag RFID</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {identifierType !== 'RFID' && (
        <div>
          <Label>CPF</Label>
          <Input
            placeholder="XXX.XXX.XXX-XX"
            value={cpf}
            onChange={(e) => setCpf(e.target.value)}
          />
        </div>
      )}

      {identifierType === 'PIN' && (
        <div>
          <Label>PIN</Label>
          <Input
            type="password"
            placeholder="••••••"
            maxLength={6}
            value={pin}
            onChange={(e) => setPin(e.target.value)}
          />
        </div>
      )}

      {identifierType === 'RFID' && (
        <div>
          <Label>Tag RFID</Label>
          <Input
            placeholder="Ex: A1B2C3D4"
            value={rfidTag}
            onChange={(e) => setRfidTag(e.target.value)}
          />
        </div>
      )}

      <Button
        type="submit"
        disabled={mutation.isPending}
        className="bg-orange-600 hover:bg-orange-700"
      >
        {mutation.isPending ? 'Identificando…' : 'Identificar'}
      </Button>

      {errorMsg && (
        <p className="rounded bg-red-50 px-3 py-2 text-sm text-red-600 dark:bg-red-900/20 dark:text-red-400">
          {errorMsg}
        </p>
      )}

      {identified && (
        <div className="rounded bg-green-50 px-3 py-2 dark:bg-green-900/20">
          <p className="text-sm font-semibold text-green-700 dark:text-green-400">
            {identified.driver.name}
          </p>
          <p className="text-xs text-green-600 dark:text-green-500">
            {identified.client.tradeName ?? identified.client.companyName}
          </p>
        </div>
      )}
    </form>
  )
}
