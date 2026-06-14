package com.octane.pricing.repository;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FuelPriceRepositoryImpl implements FuelPriceRepository {

    private final FuelPriceJpaRepository jpaRepository;

    public FuelPriceRepositoryImpl(FuelPriceJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FuelPrice save(FuelPrice fuelPrice) {
        return jpaRepository.save(fuelPrice);
    }

    @Override
    public Optional<FuelPrice> findCurrent(UUID stationId, UUID fuelId) {
        return jpaRepository.findFirstByStation_IdAndFuel_IdOrderByEffectiveFromDesc(stationId, fuelId);
    }

    @Override
    public List<FuelPrice> findCurrentByStation(UUID stationId) {
        return jpaRepository.findCurrentByStationId(stationId);
    }

    @Override
    public List<FuelPrice> findHistory(UUID stationId, UUID fuelId) {
        return jpaRepository.findByStation_IdAndFuel_IdOrderByEffectiveFromDesc(stationId, fuelId);
    }
}
