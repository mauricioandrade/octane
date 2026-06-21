package com.octane.fleet.usecase.client;

import com.octane.audit.usecase.AuditService;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteFleetClientUseCase {

    private final FleetClientRepository fleetClientRepository;
    private final AuditService auditService;

    public DeleteFleetClientUseCase(FleetClientRepository fleetClientRepository,
                                    AuditService auditService) {
        this.fleetClientRepository = fleetClientRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(UUID id) {
        var client = fleetClientRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Cliente frota não encontrado"));
        client.setDeletedAt(LocalDateTime.now());
        fleetClientRepository.save(client);
        auditService.log("DELETE", "FleetClient", id, null);
    }
}
