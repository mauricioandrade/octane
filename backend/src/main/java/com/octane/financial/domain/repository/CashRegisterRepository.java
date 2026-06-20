package com.octane.financial.domain.repository;

import com.octane.financial.domain.CashRegister;
import com.octane.financial.domain.CashMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CashRegisterRepository {
    CashRegister save(CashRegister register);
    Optional<CashRegister> findById(UUID id);
    Optional<CashRegister> findOpenByStationId(UUID stationId);
    Page<CashRegister> findClosedByStationId(UUID stationId, Pageable pageable);
    CashMovement saveMovement(CashMovement movement);
    List<CashMovement> findMovementsByRegisterId(UUID registerId);
}
