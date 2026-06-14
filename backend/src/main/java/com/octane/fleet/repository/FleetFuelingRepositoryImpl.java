package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetFueling;
import com.octane.fleet.domain.repository.FleetFuelingRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FleetFuelingRepositoryImpl implements FleetFuelingRepository {

    private final FleetFuelingJpaRepository jpaRepository;

    public FleetFuelingRepositoryImpl(FleetFuelingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FleetFueling save(FleetFueling fleetFueling) {
        return jpaRepository.save(fleetFueling);
    }

    @Override
    public Optional<FleetFueling> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Integer> findLastOdometerByVehicleId(UUID vehicleId) {
        return jpaRepository.findLastOdometerByVehicleId(vehicleId);
    }

    @Override
    public List<FleetFueling> findByFilters(UUID stationId, UUID clientId, UUID vehicleId,
                                             UUID driverId, LocalDate from, LocalDate to) {
        return jpaRepository.findByFilters(stationId, clientId, vehicleId, driverId, from, to);
    }
}
