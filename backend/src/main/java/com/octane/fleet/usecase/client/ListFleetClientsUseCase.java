package com.octane.fleet.usecase.client;

import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.usecase.FleetClientResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListFleetClientsUseCase {

    private final FleetClientRepository fleetClientRepository;

    public ListFleetClientsUseCase(FleetClientRepository fleetClientRepository) {
        this.fleetClientRepository = fleetClientRepository;
    }

    public List<FleetClientResponse> execute(UUID stationId, Boolean active) {
        return fleetClientRepository.findByStationId(stationId, active).stream()
                .map(client -> CreateFleetClientUseCase.toResponse(client,
                        fleetClientRepository.sumCurrentMonthSpend(client.getId())))
                .toList();
    }
}
