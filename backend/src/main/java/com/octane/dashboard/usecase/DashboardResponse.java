package com.octane.dashboard.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record DashboardResponse(
    BigDecimal totalRevenue,
    BigDecimal totalLiters,
    long fuelingCount,
    long openServiceOrders,
    long fleetFuelingCount,
    ActiveShiftInfo activeShift,
    List<FuelBreakdown> revenueByFuel,
    List<PaymentBreakdown> revenueByPayment
) {
    public record ActiveShiftInfo(String employeeName, LocalDateTime openedAt) {}
    public record FuelBreakdown(String fuelName, BigDecimal revenue, BigDecimal liters) {}
    public record PaymentBreakdown(String paymentMethod, BigDecimal revenue, long count) {}
}
