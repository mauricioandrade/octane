package com.octane.fleet.usecase.fueling;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterFleetFuelingRequest(
        @NotNull UUID shiftId,
        @NotNull UUID nozzleId,
        BigDecimal liters,
        BigDecimal totalAmount,
        @NotBlank String paymentMethod,
        @NotNull UUID driverId,
        @NotNull UUID vehicleId,
        @NotNull @Positive Integer odometer,
        String notes
) {}
