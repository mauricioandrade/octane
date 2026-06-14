package com.octane.fleet.usecase.report;

import java.math.BigDecimal;

public record FleetConsumptionSummary(
        BigDecimal totalLiters,
        BigDecimal totalAmount,
        int count,
        int odometerAlerts
) {}
