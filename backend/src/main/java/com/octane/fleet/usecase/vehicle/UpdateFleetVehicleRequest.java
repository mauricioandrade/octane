package com.octane.fleet.usecase.vehicle;

import java.util.UUID;

public record UpdateFleetVehicleRequest(
        String model,
        UUID allowedFuelId,
        Boolean active
) {}
