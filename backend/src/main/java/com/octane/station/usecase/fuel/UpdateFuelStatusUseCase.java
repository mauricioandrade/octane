package com.octane.station.usecase.fuel;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFuelStatusUseCase {

    private final FuelRepository fuelRepository;
    private final AuditService auditService;

    public UpdateFuelStatusUseCase(FuelRepository fuelRepository, AuditService auditService) {
        this.fuelRepository = fuelRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Fuel execute(UUID id, UpdateFuelStatusRequest request) {
        var fuel = fuelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado: " + id));

        var updated = new Fuel(fuel.getId(), fuel.getName(), fuel.getUnit(),
            request.active(), fuel.getCreatedAt());
        var saved = fuelRepository.save(updated);
        auditService.log("UPDATE", "Fuel", saved.getId(), "status=" + request.active());
        return saved;
    }
}
