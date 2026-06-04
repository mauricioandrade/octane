package com.octane.fueling.repository;

import com.octane.fueling.domain.NozzleReading;
import com.octane.fueling.domain.NozzleReadingType;
import com.octane.fueling.domain.repository.NozzleReadingRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NozzleReadingRepositoryImpl implements NozzleReadingRepository {

    private final NozzleReadingJpaRepository jpaRepository;

    public NozzleReadingRepositoryImpl(NozzleReadingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public NozzleReading save(NozzleReading reading) {
        return jpaRepository.save(reading);
    }

    @Override
    public List<NozzleReading> findByShiftId(UUID shiftId) {
        return jpaRepository.findByShift_Id(shiftId);
    }

    @Override
    public Optional<NozzleReading> findByShiftIdAndNozzleIdAndType(UUID shiftId, UUID nozzleId, NozzleReadingType type) {
        return jpaRepository.findByShift_IdAndNozzle_IdAndType(shiftId, nozzleId, type);
    }
}
