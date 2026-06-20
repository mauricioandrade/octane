package com.octane.station.usecase.fuel;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteFuelUseCase {

    private final FuelRepository fuelRepository;

    public DeleteFuelUseCase(FuelRepository fuelRepository) {
        this.fuelRepository = fuelRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var fuel = fuelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado"));
        fuel.setDeletedAt(LocalDateTime.now());
        fuelRepository.save(fuel);
    }
}
