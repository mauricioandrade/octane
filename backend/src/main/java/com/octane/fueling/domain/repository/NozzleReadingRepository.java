package com.octane.fueling.domain.repository;

import com.octane.fueling.domain.NozzleReading;
import com.octane.fueling.domain.NozzleReadingType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NozzleReadingRepository {
    NozzleReading save(NozzleReading reading);
    List<NozzleReading> findByShiftId(UUID shiftId);
    Optional<NozzleReading> findByShiftIdAndNozzleIdAndType(UUID shiftId, UUID nozzleId, NozzleReadingType type);
}
