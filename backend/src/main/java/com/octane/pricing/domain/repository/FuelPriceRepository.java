package com.octane.pricing.domain.repository;

import com.octane.pricing.domain.FuelPrice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FuelPriceRepository {
    FuelPrice save(FuelPrice fuelPrice);
    Optional<FuelPrice> findCurrent(UUID stationId, UUID fuelId);
    List<FuelPrice> findCurrentByStation(UUID stationId);
    List<FuelPrice> findHistory(UUID stationId, UUID fuelId);
}
