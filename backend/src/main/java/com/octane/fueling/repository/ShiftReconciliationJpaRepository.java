package com.octane.fueling.repository;

import com.octane.fueling.domain.ShiftReconciliation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ShiftReconciliationJpaRepository extends JpaRepository<ShiftReconciliation, UUID> {
    List<ShiftReconciliation> findByShift_Id(UUID shiftId);
}
