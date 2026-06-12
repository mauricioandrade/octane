package com.octane.station.usecase.fuel;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFuelStatusUseCase {

    private final FuelRepository fuelRepository;

    public UpdateFuelStatusUseCase(FuelRepository fuelRepository) {
        this.fuelRepository = fuelRepository;
    }

    @Transactional
    public Fuel execute(UUID id, UpdateFuelStatusRequest request) {
        var fuel = fuelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Fuel not found: " + id));

        var updated = new Fuel(fuel.getId(), fuel.getName(), fuel.getUnit(),
            request.active(), fuel.getCreatedAt());
        return fuelRepository.save(updated);
    }
}
