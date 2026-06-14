package com.octane.station.repository;

import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface PumpJpaRepository extends JpaRepository<Pump, UUID> {
    List<Pump> findByStation_Id(UUID stationId);
    List<Pump> findByStation_IdAndStatus(UUID stationId, PumpStatus status);
    boolean existsByStation_IdAndNumber(UUID stationId, int number);
}
