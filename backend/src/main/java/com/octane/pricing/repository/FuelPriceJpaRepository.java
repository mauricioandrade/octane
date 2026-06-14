package com.octane.pricing.repository;

import com.octane.pricing.domain.FuelPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FuelPriceJpaRepository extends JpaRepository<FuelPrice, UUID> {
    Optional<FuelPrice> findFirstByStation_IdAndFuel_IdOrderByEffectiveFromDesc(UUID stationId, UUID fuelId);
    List<FuelPrice> findByStation_IdOrderByEffectiveFromDesc(UUID stationId);
    List<FuelPrice> findByStation_IdAndFuel_IdOrderByEffectiveFromDesc(UUID stationId, UUID fuelId);

    @Query("SELECT fp FROM FuelPrice fp WHERE fp.station.id = :stationId " +
           "AND fp.effectiveFrom = (SELECT MAX(fp2.effectiveFrom) FROM FuelPrice fp2 " +
           "WHERE fp2.station.id = :stationId AND fp2.fuel.id = fp.fuel.id)")
    List<FuelPrice> findCurrentByStationId(@Param("stationId") UUID stationId);
}
