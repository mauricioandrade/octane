package com.octane.fleet.usecase.driver;

import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.usecase.FleetDriverResponse;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindFleetDriverUseCase {

    private final FleetDriverRepository fleetDriverRepository;

    public FindFleetDriverUseCase(FleetDriverRepository fleetDriverRepository) {
        this.fleetDriverRepository = fleetDriverRepository;
    }

    public FleetDriverResponse execute(UUID id) {
        var driver = fleetDriverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado: " + id));
        return FleetDriverResponse.from(driver);
    }
}
