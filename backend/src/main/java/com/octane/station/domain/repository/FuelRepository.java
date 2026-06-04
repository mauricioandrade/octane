package com.octane.station.domain.repository;

import com.octane.station.domain.Fuel;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelRepository {
    Optional<Fuel> findById(UUID id);
    List<Fuel> findAll();
}
