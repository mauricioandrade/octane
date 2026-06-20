package com.octane.financial.usecase;

import com.octane.financial.domain.CashRegister;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CashRegisterResponse(
    UUID id, UUID stationId, String status,
    LocalDateTime openedAt, LocalDateTime closedAt,
    BigDecimal openingBalance, BigDecimal closingBalance,
    String notes, LocalDateTime createdAt
) {
    public static CashRegisterResponse from(CashRegister r) {
        return new CashRegisterResponse(
            r.getId(), r.getStation().getId(), r.getStatus().name(),
            r.getOpenedAt(), r.getClosedAt(),
            r.getOpeningBalance(), r.getClosingBalance(),
            r.getNotes(), r.getCreatedAt()
        );
    }
}
