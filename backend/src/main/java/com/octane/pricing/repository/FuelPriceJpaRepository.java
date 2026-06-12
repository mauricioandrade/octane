package com.octane.pricing.repository;

import com.octane.pricing.domain.FuelPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FuelPriceJpaRepository extends JpaRepository<FuelPrice, UUID> {
    Optional<FuelPrice> findFirstByStation_IdAndFuel_IdOrderByEffectiveFromDesc(UUID stationId, UUID fuelId);
    List<FuelPrice> findByStation_IdOrderByEffectiveFromDesc(UUID stationId);
    List<FuelPrice> findByStation_IdAndFuel_IdOrderByEffectiveFromDesc(UUID stationId, UUID fuelId);
}
