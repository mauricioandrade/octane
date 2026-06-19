package com.octane.fleet.usecase.vehicle;

import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.fleet.usecase.FleetVehicleResponse;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindFleetVehicleUseCase {

    private final FleetVehicleRepository fleetVehicleRepository;

    public FindFleetVehicleUseCase(FleetVehicleRepository fleetVehicleRepository) {
        this.fleetVehicleRepository = fleetVehicleRepository;
    }

    public FleetVehicleResponse execute(UUID id) {
        var vehicle = fleetVehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado: " + id));
        return FleetVehicleResponse.from(vehicle);
    }
}
