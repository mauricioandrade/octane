package com.octane.inventory.repository;

import com.octane.inventory.domain.TankMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface TankMovementJpaRepository extends JpaRepository<TankMovement, UUID> {
    Page<TankMovement> findByTank_IdOrderByCreatedAtDesc(UUID tankId, Pageable pageable);
}
