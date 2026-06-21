package com.octane.station.usecase.fuel;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteFuelUseCase {

    private final FuelRepository fuelRepository;
    private final AuditService auditService;

    public DeleteFuelUseCase(FuelRepository fuelRepository, AuditService auditService) {
        this.fuelRepository = fuelRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(UUID id) {
        var fuel = fuelRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado"));
        fuel.setDeletedAt(LocalDateTime.now());
        fuelRepository.save(fuel);
        auditService.log("DELETE", "Fuel", id, null);
    }
}
