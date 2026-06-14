package com.octane.fleet.usecase.client;

import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.usecase.FleetClientResponse;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFleetClientUseCase {

    private final FleetClientRepository fleetClientRepository;

    public UpdateFleetClientUseCase(FleetClientRepository fleetClientRepository) {
        this.fleetClientRepository = fleetClientRepository;
    }

    @Transactional
    public FleetClientResponse execute(UUID id, UpdateFleetClientRequest request) {
        var client = fleetClientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente de frota não encontrado: " + id));

        if (request.companyName() != null) client.setCompanyName(request.companyName());
        if (request.tradeName() != null) client.setTradeName(request.tradeName());
        if (request.monthlyLimit() != null) client.setMonthlyLimit(request.monthlyLimit());
        if (request.active() != null) client.setActive(request.active());

        client = fleetClientRepository.save(client);
        var spend = fleetClientRepository.sumCurrentMonthSpend(client.getId());
        return CreateFleetClientUseCase.toResponse(client, spend);
    }
}
