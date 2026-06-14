package com.octane.fleet.usecase.fueling;

import com.octane.fleet.domain.repository.FleetFuelingRepository;
import com.octane.fleet.usecase.FleetDriverResponse;
import com.octane.fleet.usecase.FleetFuelingResponse;
import com.octane.fleet.usecase.FleetVehicleResponse;
import com.octane.fleet.usecase.driver.CreateFleetDriverUseCase;
import com.octane.fleet.usecase.vehicle.CreateFleetVehicleUseCase;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindFleetFuelingUseCase {

    private final FleetFuelingRepository fleetFuelingRepository;

    public FindFleetFuelingUseCase(FleetFuelingRepository fleetFuelingRepository) {
        this.fleetFuelingRepository = fleetFuelingRepository;
    }

    public FleetFuelingResponse execute(UUID id) {
        var ff = fleetFuelingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Abastecimento de frota não encontrado: " + id));
        var fueling = ff.getFueling();
        FleetDriverResponse driverResponse = CreateFleetDriverUseCase.toResponse(ff.getDriver());
        FleetVehicleResponse vehicleResponse = CreateFleetVehicleUseCase.toResponse(ff.getVehicle());
        return new FleetFuelingResponse(
                ff.getId(),
                fueling.getId(),
                driverResponse,
                vehicleResponse,
                fueling.getLiters(),
                fueling.getUnitPrice(),
                fueling.getTotalAmount(),
                fueling.getPaymentMethod().name(),
                ff.getOdometer(),
                ff.getPreviousOdometer(),
                ff.isOdometerAlert(),
                fueling.getFueledAt()
        );
    }
}
