package com.octane.fleet.usecase;

import java.util.List;

public record FleetDriverIdentificationResponse(
        FleetDriverResponse driver,
        FleetClientResponse client,
        List<FleetVehicleResponse> vehicles
) {}
