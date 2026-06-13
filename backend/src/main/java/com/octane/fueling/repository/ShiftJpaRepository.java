package com.octane.fueling.repository;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;
import java.util.UUID;

interface ShiftJpaRepository extends JpaRepository<Shift, UUID>, JpaSpecificationExecutor<Shift> {
    Optional<Shift> findByStation_IdAndStatus(UUID stationId, ShiftStatus status);
}
