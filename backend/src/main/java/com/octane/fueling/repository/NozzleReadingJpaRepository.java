package com.octane.fueling.repository;

import com.octane.fueling.domain.NozzleReading;
import com.octane.fueling.domain.NozzleReadingType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface NozzleReadingJpaRepository extends JpaRepository<NozzleReading, UUID> {
    List<NozzleReading> findByShift_Id(UUID shiftId);
    Optional<NozzleReading> findByShift_IdAndNozzle_IdAndType(UUID shiftId, UUID nozzleId, NozzleReadingType type);
}
