package com.octane.report.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record SalesReportResponse(
    BigDecimal totalRevenue,
    BigDecimal totalLiters,
    long totalCount,
    List<DailySummary> daily,
    List<FuelSummary> byFuel,
    List<PaymentSummary> byPayment
) {
    public record DailySummary(LocalDate date, BigDecimal revenue, BigDecimal liters, long count) {}
    public record FuelSummary(String fuelName, BigDecimal revenue, BigDecimal liters, long count) {}
    public record PaymentSummary(String paymentMethod, BigDecimal revenue, long count) {}
}
