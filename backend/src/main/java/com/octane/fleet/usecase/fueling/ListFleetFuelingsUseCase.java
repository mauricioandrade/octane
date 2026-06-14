package com.octane.fleet.usecase.fueling;

import com.octane.fleet.domain.FleetFueling;
import com.octane.fleet.domain.repository.FleetFuelingRepository;
import com.octane.fleet.usecase.FleetDriverResponse;
import com.octane.fleet.usecase.FleetFuelingResponse;
import com.octane.fleet.usecase.FleetVehicleResponse;
import com.octane.fleet.usecase.driver.CreateFleetDriverUseCase;
import com.octane.fleet.usecase.vehicle.CreateFleetVehicleUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListFleetFuelingsUseCase {

    private final FleetFuelingRepository fleetFuelingRepository;

    public ListFleetFuelingsUseCase(FleetFuelingRepository fleetFuelingRepository) {
        this.fleetFuelingRepository = fleetFuelingRepository;
    }

    public List<FleetFuelingResponse> execute(UUID stationId, UUID clientId, UUID vehicleId,
                                               UUID driverId, LocalDate from, LocalDate to) {
        return fleetFuelingRepository.findByFilters(stationId, clientId, vehicleId, driverId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private FleetFuelingResponse toResponse(FleetFueling ff) {
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
