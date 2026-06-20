package com.octane.financial.usecase;

import com.octane.financial.domain.CashMovement;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CashMovementResponse(
    UUID id, String type, String category, String description,
    BigDecimal amount, String paymentMethod, LocalDateTime createdAt
) {
    public static CashMovementResponse from(CashMovement m) {
        return new CashMovementResponse(
            m.getId(), m.getType().name(), m.getCategory().name(),
            m.getDescription(), m.getAmount(), m.getPaymentMethod(),
            m.getCreatedAt()
        );
    }
}
