package com.octane.station.usecase.pump;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdatePumpStatusUseCase {

    private final PumpRepository pumpRepository;
    private final AuditService auditService;

    public UpdatePumpStatusUseCase(PumpRepository pumpRepository, AuditService auditService) {
        this.pumpRepository = pumpRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Pump execute(UUID id, UpdatePumpStatusRequest request) {
        var pump = pumpRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Bomba não encontrada: " + id));

        PumpStatus status = PumpStatus.valueOf(request.status());

        var updated = new Pump(pump.getId(), pump.getNumber(), status,
            pump.getStation(), pump.getCreatedAt(), LocalDateTime.now());
        var saved = pumpRepository.save(updated);
        auditService.log("UPDATE", "Pump", saved.getId(), "status=" + status);
        return saved;
    }
}
