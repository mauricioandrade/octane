package com.octane.station.usecase.nozzle;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateNozzleStatusUseCase {

    private final NozzleRepository nozzleRepository;
    private final AuditService auditService;

    public UpdateNozzleStatusUseCase(NozzleRepository nozzleRepository, AuditService auditService) {
        this.nozzleRepository = nozzleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Nozzle execute(UUID id, UpdateNozzleStatusRequest request) {
        var nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Bico não encontrado: " + id));

        var updated = new Nozzle(nozzle.getId(), nozzle.getNumber(), nozzle.getPump(),
            nozzle.getFuel(), request.active(), nozzle.getCreatedAt(), LocalDateTime.now());
        var saved = nozzleRepository.save(updated);
        auditService.log("UPDATE", "Nozzle", saved.getId(), "status=" + request.active());
        return saved;
    }
}
