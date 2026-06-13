package com.octane.fueling.domain.repository;

import com.octane.fueling.domain.ShiftReconciliation;

import java.util.List;
import java.util.UUID;

public interface ShiftReconciliationRepository {
    List<ShiftReconciliation> saveAll(List<ShiftReconciliation> reconciliations);
    List<ShiftReconciliation> findByShiftId(UUID shiftId);
}
