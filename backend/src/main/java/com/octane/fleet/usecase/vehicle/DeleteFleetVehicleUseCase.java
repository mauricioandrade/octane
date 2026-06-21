package com.octane.fleet.usecase.vehicle;

import com.octane.audit.usecase.AuditService;
import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteFleetVehicleUseCase {

    private final FleetVehicleRepository fleetVehicleRepository;
    private final AuditService auditService;

    public DeleteFleetVehicleUseCase(FleetVehicleRepository fleetVehicleRepository,
                                     AuditService auditService) {
        this.fleetVehicleRepository = fleetVehicleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(UUID id) {
        var vehicle = fleetVehicleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
        vehicle.setDeletedAt(LocalDateTime.now());
        fleetVehicleRepository.save(vehicle);
        auditService.log("DELETE", "FleetVehicle", id, null);
    }
}
