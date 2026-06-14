package com.octane.fleet.usecase;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FleetFuelingResponse(
        UUID id,
        UUID fuelingId,
        FleetDriverResponse driver,
        FleetVehicleResponse vehicle,
        BigDecimal liters,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String paymentMethod,
        Integer odometer,
        Integer previousOdometer,
        boolean odometerAlert,
        LocalDateTime fueledAt
) {}
