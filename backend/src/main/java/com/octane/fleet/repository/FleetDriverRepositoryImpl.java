package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetDriver;
import com.octane.fleet.domain.repository.FleetDriverRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FleetDriverRepositoryImpl implements FleetDriverRepository {

    private final FleetDriverJpaRepository jpaRepository;

    public FleetDriverRepositoryImpl(FleetDriverJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FleetDriver save(FleetDriver driver) {
        return jpaRepository.save(driver);
    }

    @Override
    public Optional<FleetDriver> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<FleetDriver> findByClientId(UUID clientId, Boolean active) {
        if (active == null) {
            return jpaRepository.findByClient_Id(clientId);
        }
        return jpaRepository.findByClient_IdAndActive(clientId, active);
    }

    @Override
    public Optional<FleetDriver> findByCpfAndClientId(String cpf, UUID clientId) {
        return jpaRepository.findByCpfAndClient_Id(cpf, clientId);
    }

    @Override
    public List<FleetDriver> findByRfidTagAndStationId(String rfidTag, UUID stationId) {
        return jpaRepository.findByRfidTagAndStationId(rfidTag, stationId);
    }

    @Override
    public List<FleetDriver> findByCpfAndStationId(String cpf, UUID stationId) {
        return jpaRepository.findByCpfAndStationId(cpf, stationId);
    }
}
