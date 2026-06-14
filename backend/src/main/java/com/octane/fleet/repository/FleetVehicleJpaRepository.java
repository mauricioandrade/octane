package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetVehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FleetVehicleJpaRepository extends JpaRepository<FleetVehicle, UUID> {

    List<FleetVehicle> findByClient_Id(UUID clientId);

    List<FleetVehicle> findByClient_IdAndActive(UUID clientId, boolean active);

    Optional<FleetVehicle> findByPlate(String plate);
}
