package com.octane.fleet.usecase;

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
) {}
