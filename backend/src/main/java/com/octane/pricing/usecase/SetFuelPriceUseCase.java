package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SetFuelPriceUseCase {

    private final FuelPriceRepository fuelPriceRepository;
    private final StationRepository stationRepository;
    private final FuelRepository fuelRepository;

    public SetFuelPriceUseCase(FuelPriceRepository fuelPriceRepository,
                               StationRepository stationRepository,
                               FuelRepository fuelRepository) {
        this.fuelPriceRepository = fuelPriceRepository;
        this.stationRepository = stationRepository;
        this.fuelRepository = fuelRepository;
    }

    @Transactional
    public FuelPrice execute(UUID stationId, SetFuelPriceRequest request) {
        var station = stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + stationId));

        if (!station.isActive()) {
            throw new BusinessException("Posto inativo: não é possível cadastrar preço");
        }

        var fuel = fuelRepository.findById(request.fuelId())
            .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado: " + request.fuelId()));

        if (!fuel.isActive()) {
            throw new BusinessException("Combustível inativo: " + fuel.getName());
        }

        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Preço deve ser maior que zero");
        }

        var now = LocalDateTime.now();
        var fuelPrice = new FuelPrice(null, station, fuel, request.price(), now, now);
        return fuelPriceRepository.save(fuelPrice);
    }
}
