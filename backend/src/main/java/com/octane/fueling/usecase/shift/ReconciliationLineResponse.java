package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.ShiftReconciliation;

import java.math.BigDecimal;
import java.util.UUID;

public record ReconciliationLineResponse(
    UUID nozzleId,
    int nozzleNumber,
    String fuelName,
    BigDecimal openingTotalizer,
    BigDecimal closingTotalizer,
    BigDecimal measuredLiters,
    BigDecimal fueledLiters,
    BigDecimal divergenceLiters
) {
    public static ReconciliationLineResponse from(ShiftReconciliation reconciliation) {
        return new ReconciliationLineResponse(
            reconciliation.getNozzle().getId(),
            reconciliation.getNozzle().getNumber(),
            reconciliation.getNozzle().getFuel().getName(),
            reconciliation.getOpeningTotalizer(),
            reconciliation.getClosingTotalizer(),
            reconciliation.getMeasuredLiters(),
            reconciliation.getFueledLiters(),
            reconciliation.getDivergenceLiters()
        );
    }
}
