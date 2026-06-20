package com.octane.report.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ShiftReportResponse(
    List<ShiftLine> shifts,
    BigDecimal totalRevenue,
    BigDecimal totalLiters,
    long totalFuelings
) {
    public record ShiftLine(
        String employeeName,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        long durationMinutes,
        BigDecimal revenue,
        BigDecimal liters,
        long fuelingCount
    ) {}
}
