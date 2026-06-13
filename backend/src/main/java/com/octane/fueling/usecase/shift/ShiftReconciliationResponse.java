package com.octane.fueling.usecase.shift;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShiftReconciliationResponse(
    UUID shiftId,
    List<ReconciliationLineResponse> lines,
    BigDecimal totalMeasuredLiters,
    BigDecimal totalFueledLiters,
    BigDecimal totalDivergenceLiters
) {}
