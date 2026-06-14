package com.octane.fueling.usecase.fueling;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record RegisterFuelingRequest(
        @NotNull UUID nozzleId,
        @NotNull @Positive BigDecimal liters,
        @NotNull @Positive BigDecimal totalAmount,
        @NotBlank String paymentMethod,
        String vehiclePlate,
        String notes
) {}
