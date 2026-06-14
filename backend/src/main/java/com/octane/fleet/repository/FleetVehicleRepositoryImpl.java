package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetVehicle;
import com.octane.fleet.domain.repository.FleetVehicleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FleetVehicleRepositoryImpl implements FleetVehicleRepository {

    private final FleetVehicleJpaRepository jpaRepository;

    public FleetVehicleRepositoryImpl(FleetVehicleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FleetVehicle save(FleetVehicle vehicle) {
        return jpaRepository.save(vehicle);
    }

    @Override
    public Optional<FleetVehicle> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<FleetVehicle> findByClientId(UUID clientId, Boolean active) {
        if (active == null) {
            return jpaRepository.findByClient_Id(clientId);
        }
        return jpaRepository.findByClient_IdAndActive(clientId, active);
    }

    @Override
    public Optional<FleetVehicle> findByPlate(String plate) {
        return jpaRepository.findByPlate(plate);
    }
}
