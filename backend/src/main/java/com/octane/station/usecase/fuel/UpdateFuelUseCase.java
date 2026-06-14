package com.octane.station.usecase.fuel;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.shared.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFuelUseCase {

    private final FuelRepository fuelRepository;

    public UpdateFuelUseCase(FuelRepository fuelRepository) {
        this.fuelRepository = fuelRepository;
    }

    @Transactional
    public Fuel execute(UUID id, UpdateFuelRequest request) {
        var fuel = fuelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Fuel not found: " + id));

        String newName = request.name() != null ? request.name() : fuel.getName();
        FuelUnit newUnit = fuel.getUnit();

        if (request.unit() != null) {
            try {
                newUnit = FuelUnit.valueOf(request.unit());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Unidade inválida: " + request.unit());
            }
        }

        var updated = new Fuel(fuel.getId(), newName, newUnit, fuel.isActive(), fuel.getCreatedAt());
        return fuelRepository.save(updated);
    }
}
