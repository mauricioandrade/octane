package com.octane.fleet.usecase.client;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.usecase.FleetClientResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class ListFleetClientsUseCase {

    private final FleetClientRepository fleetClientRepository;

    public ListFleetClientsUseCase(FleetClientRepository fleetClientRepository) {
        this.fleetClientRepository = fleetClientRepository;
    }

    public List<FleetClientResponse> execute(UUID stationId, Boolean active) {
        var clients = fleetClientRepository.findByStationId(stationId, active);
        var spendByClient = fleetClientRepository.sumCurrentMonthSpendByClientIds(
                clients.stream().map(FleetClient::getId).toList());
        return clients.stream()
                .map(client -> FleetClientResponse.from(client,
                        spendByClient.getOrDefault(client.getId(), BigDecimal.ZERO)))
                .toList();
    }
}
