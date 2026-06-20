package com.octane.fleet.usecase.vehicle;

import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteFleetVehicleUseCase {

    private final FleetVehicleRepository fleetVehicleRepository;

    public DeleteFleetVehicleUseCase(FleetVehicleRepository fleetVehicleRepository) {
        this.fleetVehicleRepository = fleetVehicleRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var vehicle = fleetVehicleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado"));
        vehicle.setDeletedAt(LocalDateTime.now());
        fleetVehicleRepository.save(vehicle);
    }
}
