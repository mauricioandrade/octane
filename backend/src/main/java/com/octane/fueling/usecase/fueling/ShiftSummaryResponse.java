package com.octane.fueling.usecase.fueling;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record ShiftSummaryResponse(
        UUID shiftId,
        List<FuelingResponse> fuelings,
        BigDecimal totalLiters,
        BigDecimal totalAmount
) {}
