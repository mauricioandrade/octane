package com.octane.commission.usecase;

import com.octane.commission.domain.CommissionEntry;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommissionEntryResponse(
        UUID id,
        UUID shiftId,
        String employeeName,
        UUID stationId,
        String stationName,
        BigDecimal baseAmount,
        BigDecimal rate,
        BigDecimal commission,
        boolean paid,
        LocalDateTime paidAt,
        LocalDateTime createdAt
) {
    public static CommissionEntryResponse from(CommissionEntry entry) {
        return new CommissionEntryResponse(
                entry.getId(),
                entry.getShift().getId(),
                entry.getEmployeeName(),
                entry.getStation().getId(),
                entry.getStation().getName(),
                entry.getBaseAmount(),
                entry.getRate(),
                entry.getCommission(),
                entry.isPaid(),
                entry.getPaidAt(),
                entry.getCreatedAt()
        );
    }
}
