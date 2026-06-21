package com.octane.station.usecase.pump;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeletePumpUseCase {

    private final PumpRepository pumpRepository;
    private final AuditService auditService;

    public DeletePumpUseCase(PumpRepository pumpRepository, AuditService auditService) {
        this.pumpRepository = pumpRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(UUID id) {
        var pump = pumpRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Bomba não encontrada"));
        pump.setDeletedAt(LocalDateTime.now());
        pumpRepository.save(pump);
        auditService.log("DELETE", "Pump", id, null);
    }
}
