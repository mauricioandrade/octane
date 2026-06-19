package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetFueling;

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
) {
    public static FleetFuelingResponse from(FleetFueling ff) {
        var fueling = ff.getFueling();
        return new FleetFuelingResponse(
                ff.getId(),
                fueling.getId(),
                FleetDriverResponse.from(ff.getDriver()),
                FleetVehicleResponse.from(ff.getVehicle()),
                fueling.getLiters(),
                fueling.getUnitPrice(),
                fueling.getTotalAmount(),
                fueling.getPaymentMethod().name(),
                ff.getOdometer(),
                ff.getPreviousOdometer(),
                ff.isOdometerAlert(),
                fueling.getFueledAt()
        );
    }
}
