package com.octane.inventory.usecase;

import com.octane.inventory.domain.Tank;
import java.math.BigDecimal;
import java.util.UUID;

public record TankResponse(
    UUID id, UUID stationId, UUID fuelId, String fuelName,
    String name, BigDecimal capacity, BigDecimal currentLevel,
    BigDecimal minimumLevel, boolean active, boolean belowMinimum
) {
    public static TankResponse from(Tank t) {
        return new TankResponse(
            t.getId(), t.getStation().getId(), t.getFuel().getId(), t.getFuel().getName(),
            t.getName(), t.getCapacity(), t.getCurrentLevel(), t.getMinimumLevel(),
            t.isActive(), t.getCurrentLevel().compareTo(t.getMinimumLevel()) < 0
        );
    }
}
