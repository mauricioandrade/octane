package com.octane.fleet.usecase.driver;

import com.octane.audit.usecase.AuditService;
import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteFleetDriverUseCase {

    private final FleetDriverRepository fleetDriverRepository;
    private final AuditService auditService;

    public DeleteFleetDriverUseCase(FleetDriverRepository fleetDriverRepository,
                                    AuditService auditService) {
        this.fleetDriverRepository = fleetDriverRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(UUID id) {
        var driver = fleetDriverRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado"));
        driver.setDeletedAt(LocalDateTime.now());
        fleetDriverRepository.save(driver);
        auditService.log("DELETE", "FleetDriver", id, null);
    }
}
