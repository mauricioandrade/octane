package com.octane.inventory.repository;

import com.octane.inventory.domain.Tank;
import com.octane.inventory.domain.TankMovement;
import com.octane.inventory.domain.repository.TankRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class TankRepositoryImpl implements TankRepository {

    private final TankJpaRepository tankJpa;
    private final TankMovementJpaRepository movementJpa;

    public TankRepositoryImpl(TankJpaRepository tankJpa, TankMovementJpaRepository movementJpa) {
        this.tankJpa = tankJpa;
        this.movementJpa = movementJpa;
    }

    @Override public Tank save(Tank tank) { return tankJpa.save(tank); }
    @Override public Optional<Tank> findById(UUID id) { return tankJpa.findById(id); }
    @Override public List<Tank> findByStationId(UUID stationId) { return tankJpa.findByStation_IdOrderByNameAsc(stationId); }
    @Override public Optional<Tank> findByStationIdAndFuelId(UUID stationId, UUID fuelId) {
        return tankJpa.findFirstByStation_IdAndFuel_IdAndActiveTrue(stationId, fuelId);
    }
    @Override public TankMovement saveMovement(TankMovement m) { return movementJpa.save(m); }
    @Override public Page<TankMovement> findMovementsByTankId(UUID tankId, Pageable p) {
        return movementJpa.findByTank_IdOrderByCreatedAtDesc(tankId, p);
    }
}
