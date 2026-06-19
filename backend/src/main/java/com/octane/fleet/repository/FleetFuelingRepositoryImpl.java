package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetFueling;
import com.octane.fleet.domain.repository.FleetFuelingRepository;
import com.octane.shared.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
    public PageResponse<FleetFueling> findByFilters(UUID stationId, UUID clientId, UUID vehicleId,
                                                     UUID driverId, LocalDate from, LocalDate to,
                                                     int page, int size) {
        Page<FleetFueling> result = jpaRepository.findByFilters(stationId, clientId, vehicleId, driverId,
                from, to, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "fueling.fueledAt")));
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }

    @Override
    public List<FleetFueling> findAllByFilters(UUID stationId, UUID clientId, UUID vehicleId,
                                                UUID driverId, LocalDate from, LocalDate to) {
        return jpaRepository.findAllByFilters(stationId, clientId, vehicleId, driverId, from, to);
    }
}
