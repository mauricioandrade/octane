package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FuelingResponse(
        UUID id,
        UUID nozzleId,
        int nozzleNumber,
        String fuelName,
        BigDecimal liters,
        BigDecimal unitPrice,
        BigDecimal totalAmount,
        String paymentMethod,
        String vehiclePlate,
        LocalDateTime fueledAt
) {
    public static FuelingResponse from(Fueling fueling) {
        return new FuelingResponse(
                fueling.getId(),
                fueling.getNozzle().getId(),
                fueling.getNozzle().getNumber(),
                fueling.getNozzle().getFuel().getName(),
                fueling.getLiters(),
                fueling.getUnitPrice(),
                fueling.getTotalAmount(),
                fueling.getPaymentMethod().name(),
                fueling.getVehiclePlate(),
                fueling.getFueledAt()
        );
    }
}
