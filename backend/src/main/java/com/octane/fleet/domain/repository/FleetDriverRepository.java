package com.octane.fleet.domain.repository;

import com.octane.fleet.domain.FleetDriver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FleetDriverRepository {
    FleetDriver save(FleetDriver driver);
    Optional<FleetDriver> findById(UUID id);
    List<FleetDriver> findByClientId(UUID clientId, Boolean active);
    Optional<FleetDriver> findByCpfAndClientId(String cpf, UUID clientId);
    List<FleetDriver> findByRfidTagAndStationId(String rfidTag, UUID stationId);
    List<FleetDriver> findByCpfAndStationId(String cpf, UUID stationId);
}
