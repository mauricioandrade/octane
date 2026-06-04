package com.octane.fueling.repository;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ShiftJpaRepository extends JpaRepository<Shift, UUID> {
    Optional<Shift> findByStation_IdAndStatus(UUID stationId, ShiftStatus status);
    List<Shift> findByStation_Id(UUID stationId);
}
