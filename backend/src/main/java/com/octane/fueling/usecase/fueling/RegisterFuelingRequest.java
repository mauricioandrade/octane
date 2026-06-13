package com.octane.fueling.usecase.fueling;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterFuelingRequest(
        UUID nozzleId,
        BigDecimal liters,
        BigDecimal totalAmount,
        String paymentMethod,
        String vehiclePlate,
        String notes
) {}
