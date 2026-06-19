package com.octane.fleet.domain.repository;

import com.octane.fleet.domain.FleetFueling;
import com.octane.shared.pagination.PageResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FleetFuelingRepository {
    FleetFueling save(FleetFueling fleetFueling);
    Optional<FleetFueling> findById(UUID id);
    Optional<Integer> findLastOdometerByVehicleId(UUID vehicleId);
    PageResponse<FleetFueling> findByFilters(UUID stationId, UUID clientId, UUID vehicleId, UUID driverId,
                                              LocalDate from, LocalDate to, int page, int size);
    List<FleetFueling> findAllByFilters(UUID stationId, UUID clientId, UUID vehicleId, UUID driverId,
                                         LocalDate from, LocalDate to);
}
