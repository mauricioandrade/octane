package com.octane.station.usecase.nozzle;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteNozzleUseCase {

    private final NozzleRepository nozzleRepository;
    private final AuditService auditService;

    public DeleteNozzleUseCase(NozzleRepository nozzleRepository, AuditService auditService) {
        this.nozzleRepository = nozzleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(UUID id) {
        var nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Bico não encontrado"));
        nozzle.setDeletedAt(LocalDateTime.now());
        nozzleRepository.save(nozzle);
        auditService.log("DELETE", "Nozzle", id, null);
    }
}
