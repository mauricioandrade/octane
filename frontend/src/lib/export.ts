import type { ShiftReconciliation } from '@/types'

export function exportReconciliationCSV(reconciliation: ShiftReconciliation, filename: string) {
  const rows: string[][] = [
    ['Bico', 'Combustível', 'Medido (L)', 'Lançado (L)', 'Divergência (L)'],
    ...reconciliation.lines.map((l) => [
      `B${l.nozzleNumber}`,
      l.fuelName,
      l.measuredLiters.toFixed(3),
      l.fueledLiters.toFixed(3),
      l.divergenceLiters.toFixed(3),
    ]),
    ['Total', '', reconciliation.totalMeasuredLiters.toFixed(3), '', ''],
  ]
  const csv = rows.map((r) => r.map((c) => `"${c}"`).join(';')).join('\r\n')
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  a.click()
  URL.revokeObjectURL(url)
}
