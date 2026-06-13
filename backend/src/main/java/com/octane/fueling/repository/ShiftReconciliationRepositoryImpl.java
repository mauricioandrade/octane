package com.octane.fueling.repository;

import com.octane.fueling.domain.ShiftReconciliation;
import com.octane.fueling.domain.repository.ShiftReconciliationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class ShiftReconciliationRepositoryImpl implements ShiftReconciliationRepository {

    private final ShiftReconciliationJpaRepository jpaRepository;

    public ShiftReconciliationRepositoryImpl(ShiftReconciliationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<ShiftReconciliation> saveAll(List<ShiftReconciliation> reconciliations) {
        return jpaRepository.saveAll(reconciliations);
    }

    @Override
    public List<ShiftReconciliation> findByShiftId(UUID shiftId) {
        return jpaRepository.findByShift_Id(shiftId);
    }
}
