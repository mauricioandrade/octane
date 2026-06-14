package com.octane.commission.repository;

import com.octane.commission.domain.CommissionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

interface CommissionEntryJpaRepository extends JpaRepository<CommissionEntry, UUID>,
        JpaSpecificationExecutor<CommissionEntry> {
    Optional<CommissionEntry> findByShift_Id(UUID shiftId);
}
