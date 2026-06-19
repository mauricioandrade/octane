package com.octane.fleet.usecase.client;

import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.usecase.FleetClientResponse;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindFleetClientUseCase {

    private final FleetClientRepository fleetClientRepository;

    public FindFleetClientUseCase(FleetClientRepository fleetClientRepository) {
        this.fleetClientRepository = fleetClientRepository;
    }

    public FleetClientResponse execute(UUID id) {
        var client = fleetClientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente de frota não encontrado: " + id));
        var spend = fleetClientRepository.sumCurrentMonthSpend(id);
        return FleetClientResponse.from(client, spend);
    }
}
