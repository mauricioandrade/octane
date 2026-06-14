package com.octane.fleet.domain.repository;

import com.octane.fleet.domain.FleetFueling;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FleetFuelingRepository {
    FleetFueling save(FleetFueling fleetFueling);
    Optional<FleetFueling> findById(UUID id);
    Optional<Integer> findLastOdometerByVehicleId(UUID vehicleId);
    List<FleetFueling> findByFilters(UUID stationId, UUID clientId, UUID vehicleId, UUID driverId,
                                     LocalDate from, LocalDate to);
}
