package com.octane.station.usecase.nozzle;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateNozzleUseCase {

    private final NozzleRepository nozzleRepository;
    private final FuelRepository fuelRepository;
    private final AuditService auditService;

    public UpdateNozzleUseCase(NozzleRepository nozzleRepository, FuelRepository fuelRepository,
                               AuditService auditService) {
        this.nozzleRepository = nozzleRepository;
        this.fuelRepository = fuelRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Nozzle execute(UUID id, UpdateNozzleRequest request) {
        var nozzle = nozzleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Bico não encontrado: " + id));

        var fuel = fuelRepository.findById(request.fuelId())
            .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado: " + request.fuelId()));

        if (!fuel.isActive()) {
            throw new BusinessException("Combustível inativo: " + fuel.getName());
        }

        if (request.number() != nozzle.getNumber()
                && nozzleRepository.existsByPumpIdAndNumber(nozzle.getPump().getId(), request.number())) {
            throw new BusinessException("Bico número " + request.number() + " já existe nesta bomba");
        }

        var updated = new Nozzle(nozzle.getId(), request.number(), nozzle.getPump(), fuel,
            nozzle.isActive(), nozzle.getCreatedAt(), LocalDateTime.now());
        var saved = nozzleRepository.save(updated);
        auditService.log("UPDATE", "Nozzle", saved.getId(), "bico " + saved.getNumber());
        return saved;
    }
}
