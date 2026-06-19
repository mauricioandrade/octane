package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetClient;

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
) {
    public static FleetClientResponse from(FleetClient client, BigDecimal currentMonthSpend) {
        return new FleetClientResponse(
                client.getId(),
                client.getStation().getId(),
                client.getCnpj(),
                client.getCompanyName(),
                client.getTradeName(),
                client.getMonthlyLimit(),
                currentMonthSpend,
                client.isActive(),
                client.getCreatedAt()
        );
    }
}
