package com.octane.station.repository;

import com.octane.station.domain.Nozzle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface NozzleJpaRepository extends JpaRepository<Nozzle, UUID> {
    List<Nozzle> findByPump_Id(UUID pumpId);
    boolean existsByPump_IdAndNumber(UUID pumpId, int number);
}
