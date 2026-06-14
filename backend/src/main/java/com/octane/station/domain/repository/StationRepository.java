package com.octane.station.domain.repository;

import com.octane.station.domain.Station;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StationRepository {
    Station save(Station station);
    Optional<Station> findById(UUID id);
    Optional<Station> findByCnpj(String cnpj);
    List<Station> findAll();
    List<Station> findAll(Boolean active);
}
