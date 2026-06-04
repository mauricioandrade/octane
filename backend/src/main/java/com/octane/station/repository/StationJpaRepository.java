package com.octane.station.repository;

import com.octane.station.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface StationJpaRepository extends JpaRepository<Station, UUID> {
    Optional<Station> findByCnpj(String cnpj);
}
