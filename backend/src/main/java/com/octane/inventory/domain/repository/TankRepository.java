package com.octane.inventory.domain.repository;

import com.octane.inventory.domain.Tank;
import com.octane.inventory.domain.TankMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TankRepository {
    Tank save(Tank tank);
    Optional<Tank> findById(UUID id);
    List<Tank> findByStationId(UUID stationId);
    Optional<Tank> findByStationIdAndFuelId(UUID stationId, UUID fuelId);
    TankMovement saveMovement(TankMovement movement);
    Page<TankMovement> findMovementsByTankId(UUID tankId, Pageable pageable);
}
