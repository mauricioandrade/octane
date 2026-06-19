package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetVehicle;

import java.time.LocalDateTime;
import java.util.UUID;

public record FleetVehicleResponse(
        UUID id,
        UUID clientId,
        String plate,
        String model,
        UUID allowedFuelId,
        String allowedFuelName,
        boolean active,
        LocalDateTime createdAt
) {
    public static FleetVehicleResponse from(FleetVehicle vehicle) {
        return new FleetVehicleResponse(
                vehicle.getId(),
                vehicle.getClient().getId(),
                vehicle.getPlate(),
                vehicle.getModel(),
                vehicle.getAllowedFuel().getId(),
                vehicle.getAllowedFuel().getName(),
                vehicle.isActive(),
                vehicle.getCreatedAt()
        );
    }
}
