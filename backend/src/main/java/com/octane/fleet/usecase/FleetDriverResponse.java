package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetDriver;

import java.time.LocalDateTime;
import java.util.UUID;

public record FleetDriverResponse(
        UUID id,
        UUID clientId,
        String name,
        String cpf,
        boolean hasPIN,
        boolean hasRFID,
        boolean active,
        LocalDateTime createdAt
) {
    public static FleetDriverResponse from(FleetDriver driver) {
        return new FleetDriverResponse(
                driver.getId(),
                driver.getClient().getId(),
                driver.getName(),
                driver.getCpf(),
                driver.getPinHash() != null,
                driver.getRfidTag() != null,
                driver.isActive(),
                driver.getCreatedAt()
        );
    }
}
