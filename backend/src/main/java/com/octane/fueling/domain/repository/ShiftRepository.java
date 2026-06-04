package com.octane.fueling.domain.repository;

import com.octane.fueling.domain.Shift;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShiftRepository {
    Shift save(Shift shift);
    Optional<Shift> findById(UUID id);
    Optional<Shift> findOpenByStationId(UUID stationId);
    List<Shift> findByStationId(UUID stationId);
}
