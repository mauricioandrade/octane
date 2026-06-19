package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListPriceHistoryUseCase {

    private final FuelPriceRepository fuelPriceRepository;
    private final StationRepository stationRepository;

    public ListPriceHistoryUseCase(FuelPriceRepository fuelPriceRepository,
                                   StationRepository stationRepository) {
        this.fuelPriceRepository = fuelPriceRepository;
        this.stationRepository = stationRepository;
    }

    public List<FuelPrice> execute(UUID stationId, UUID fuelId) {
        stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + stationId));
        return fuelPriceRepository.findHistory(stationId, fuelId);
    }
}
