package com.octane.financial.repository;

import com.octane.financial.domain.CashRegister;
import com.octane.financial.domain.CashRegisterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface CashRegisterJpaRepository extends JpaRepository<CashRegister, UUID> {
    Optional<CashRegister> findByStation_IdAndStatus(UUID stationId, CashRegisterStatus status);
    Page<CashRegister> findByStation_IdAndStatusOrderByClosedAtDesc(UUID stationId, CashRegisterStatus status, Pageable pageable);
}
