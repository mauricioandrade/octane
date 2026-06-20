package com.octane.inventory.usecase;

import com.octane.inventory.domain.TankMovement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TankMovementResponse(
    UUID id, String type, BigDecimal volumeLiters,
    BigDecimal previousLevel, BigDecimal newLevel,
    String notes, LocalDateTime createdAt
) {
    public static TankMovementResponse from(TankMovement m) {
        return new TankMovementResponse(
            m.getId(), m.getType().name(), m.getVolumeLiters(),
            m.getPreviousLevel(), m.getNewLevel(),
            m.getNotes(), m.getCreatedAt()
        );
    }
}
