package com.octane.fleet.usecase.driver;

import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteFleetDriverUseCase {

    private final FleetDriverRepository fleetDriverRepository;

    public DeleteFleetDriverUseCase(FleetDriverRepository fleetDriverRepository) {
        this.fleetDriverRepository = fleetDriverRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var driver = fleetDriverRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado"));
        driver.setDeletedAt(LocalDateTime.now());
        fleetDriverRepository.save(driver);
    }
}
