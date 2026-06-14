package com.octane.fleet.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FleetClientResponse(
        UUID id,
        UUID stationId,
        String cnpj,
        String companyName,
        String tradeName,
        BigDecimal monthlyLimit,
        BigDecimal currentMonthSpend,
        boolean active,
        LocalDateTime createdAt
) {}
