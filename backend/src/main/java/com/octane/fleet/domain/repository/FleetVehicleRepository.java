package com.octane.fleet.domain.repository;

import com.octane.fleet.domain.FleetVehicle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FleetVehicleRepository {
    FleetVehicle save(FleetVehicle vehicle);
    Optional<FleetVehicle> findById(UUID id);
    List<FleetVehicle> findByClientId(UUID clientId, Boolean active);
    Optional<FleetVehicle> findByPlate(String plate);
}
