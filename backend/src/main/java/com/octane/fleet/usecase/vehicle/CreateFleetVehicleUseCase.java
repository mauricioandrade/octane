package com.octane.fleet.usecase.vehicle;

import com.octane.audit.usecase.AuditService;
import com.octane.fleet.domain.FleetVehicle;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.fleet.usecase.FleetVehicleResponse;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateFleetVehicleUseCase {

    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetClientRepository fleetClientRepository;
    private final FuelRepository fuelRepository;
    private final AuditService auditService;

    public CreateFleetVehicleUseCase(FleetVehicleRepository fleetVehicleRepository,
                                     FleetClientRepository fleetClientRepository,
                                     FuelRepository fuelRepository,
                                     AuditService auditService) {
        this.fleetVehicleRepository = fleetVehicleRepository;
        this.fleetClientRepository = fleetClientRepository;
        this.fuelRepository = fuelRepository;
        this.auditService = auditService;
    }

    @Transactional
    public FleetVehicleResponse execute(CreateFleetVehicleRequest request) {
        var clientId = request.clientId();
        var client = fleetClientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente de frota não encontrado: " + clientId));

        fleetVehicleRepository.findByPlate(request.plate())
                .ifPresent(v -> { throw new BusinessException("Placa já cadastrada em outra frota"); });

        var fuelId = request.allowedFuelId();
        var fuel = fuelRepository.findById(fuelId)
                .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado: " + fuelId));

        if (!fuel.isActive()) {
            throw new BusinessException("Combustível inativo");
        }

        var vehicle = new FleetVehicle(null, client, request.plate(), request.model(),
                fuel, true, LocalDateTime.now());
        vehicle = fleetVehicleRepository.save(vehicle);
        auditService.log("CREATE", "FleetVehicle", vehicle.getId(), vehicle.getPlate());
        return FleetVehicleResponse.from(vehicle);
    }
}
