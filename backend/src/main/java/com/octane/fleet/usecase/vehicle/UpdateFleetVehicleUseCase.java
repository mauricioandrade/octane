package com.octane.fleet.usecase.vehicle;

import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.fleet.usecase.FleetVehicleResponse;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFleetVehicleUseCase {

    private final FleetVehicleRepository fleetVehicleRepository;
    private final FuelRepository fuelRepository;

    public UpdateFleetVehicleUseCase(FleetVehicleRepository fleetVehicleRepository,
                                     FuelRepository fuelRepository) {
        this.fleetVehicleRepository = fleetVehicleRepository;
        this.fuelRepository = fuelRepository;
    }

    @Transactional
    public FleetVehicleResponse execute(UUID id, UpdateFleetVehicleRequest request) {
        var vehicle = fleetVehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Veículo de frota não encontrado: " + id));

        if (request.model() != null) vehicle.setModel(request.model());
        if (request.active() != null) vehicle.setActive(request.active());
        if (request.allowedFuelId() != null) {
            var fuel = fuelRepository.findById(request.allowedFuelId())
                    .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado: " + request.allowedFuelId()));
            vehicle.setAllowedFuel(fuel);
        }

        vehicle = fleetVehicleRepository.save(vehicle);
        return CreateFleetVehicleUseCase.toResponse(vehicle);
    }
}
