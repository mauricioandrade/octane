package com.octane.fleet.usecase.report;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FleetConsumptionLine(
        LocalDateTime fueledAt,
        String clientName,
        String clientCnpj,
        String driverName,
        String driverCpf,
        String vehiclePlate,
        String vehicleModel,
        String fuelName,
        BigDecimal liters,
        BigDecimal totalAmount,
        Integer odometer,
        boolean odometerAlert,
        String paymentMethod
) {}
