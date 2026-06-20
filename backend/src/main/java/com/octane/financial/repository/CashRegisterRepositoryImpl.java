package com.octane.financial.repository;

import com.octane.financial.domain.CashMovement;
import com.octane.financial.domain.CashRegister;
import com.octane.financial.domain.CashRegisterStatus;
import com.octane.financial.domain.repository.CashRegisterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CashRegisterRepositoryImpl implements CashRegisterRepository {

    private final CashRegisterJpaRepository registerJpa;
    private final CashMovementJpaRepository movementJpa;

    public CashRegisterRepositoryImpl(CashRegisterJpaRepository registerJpa,
                                       CashMovementJpaRepository movementJpa) {
        this.registerJpa = registerJpa;
        this.movementJpa = movementJpa;
    }

    @Override
    public CashRegister save(CashRegister register) { return registerJpa.save(register); }

    @Override
    public Optional<CashRegister> findById(UUID id) { return registerJpa.findById(id); }

    @Override
    public Optional<CashRegister> findOpenByStationId(UUID stationId) {
        return registerJpa.findByStation_IdAndStatus(stationId, CashRegisterStatus.OPEN);
    }

    @Override
    public Page<CashRegister> findClosedByStationId(UUID stationId, Pageable pageable) {
        return registerJpa.findByStation_IdAndStatusOrderByClosedAtDesc(stationId, CashRegisterStatus.CLOSED, pageable);
    }

    @Override
    public CashMovement saveMovement(CashMovement movement) { return movementJpa.save(movement); }

    @Override
    public List<CashMovement> findMovementsByRegisterId(UUID registerId) {
        return movementJpa.findByCashRegister_IdOrderByCreatedAtDesc(registerId);
    }
}
