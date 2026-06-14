package com.octane.fleet.usecase.driver;

import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.usecase.FleetDriverResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListFleetDriversUseCase {

    private final FleetDriverRepository fleetDriverRepository;

    public ListFleetDriversUseCase(FleetDriverRepository fleetDriverRepository) {
        this.fleetDriverRepository = fleetDriverRepository;
    }

    public List<FleetDriverResponse> execute(UUID clientId, Boolean active) {
        return fleetDriverRepository.findByClientId(clientId, active).stream()
                .map(CreateFleetDriverUseCase::toResponse)
                .toList();
    }
}
