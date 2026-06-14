package com.octane.station.domain.repository;

import com.octane.station.domain.Nozzle;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NozzleRepository {
    Nozzle save(Nozzle nozzle);
    Optional<Nozzle> findById(UUID id);
    List<Nozzle> findByPumpId(UUID pumpId);
    List<Nozzle> findByPumpId(UUID pumpId, boolean active);
    boolean existsByPumpIdAndNumber(UUID pumpId, int number);
}
