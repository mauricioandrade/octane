package com.octane.station.domain.repository;

import com.octane.station.domain.Pump;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PumpRepository {
    Pump save(Pump pump);
    Optional<Pump> findById(UUID id);
    List<Pump> findByStationId(UUID stationId);
    boolean existsByStationIdAndNumber(UUID stationId, int number);
}
