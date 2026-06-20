package com.octane.financial.repository;

import com.octane.financial.domain.CashMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface CashMovementJpaRepository extends JpaRepository<CashMovement, UUID> {
    List<CashMovement> findByCashRegister_IdOrderByCreatedAtDesc(UUID registerId);
}
