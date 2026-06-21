package com.octane.fleet.usecase.client;

import com.octane.audit.usecase.AuditService;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.usecase.FleetClientResponse;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFleetClientUseCase {

    private final FleetClientRepository fleetClientRepository;
    private final AuditService auditService;

    public UpdateFleetClientUseCase(FleetClientRepository fleetClientRepository,
                                    AuditService auditService) {
        this.fleetClientRepository = fleetClientRepository;
        this.auditService = auditService;
    }

    @Transactional
    public FleetClientResponse execute(UUID id, UpdateFleetClientRequest request) {
        var client = fleetClientRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente de frota não encontrado: " + id));

        if (request.companyName() != null) client.setCompanyName(request.companyName());
        client.setTradeName(request.tradeName());
        client.setMonthlyLimit(request.monthlyLimit());
        if (request.active() != null) client.setActive(request.active());

        client = fleetClientRepository.save(client);
        auditService.log("UPDATE", "FleetClient", client.getId(), client.getCompanyName());
        var spend = fleetClientRepository.sumCurrentMonthSpend(client.getId());
        return FleetClientResponse.from(client, spend);
    }
}
