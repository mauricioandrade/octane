package com.octane.fueling.domain.repository;

import com.octane.fueling.domain.Fueling;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelingRepository {
    Fueling save(Fueling fueling);
    List<Fueling> findByShiftId(UUID shiftId);
    Optional<Fueling> findById(UUID id);
}
