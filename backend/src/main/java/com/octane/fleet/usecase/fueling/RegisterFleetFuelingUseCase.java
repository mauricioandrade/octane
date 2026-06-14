package com.octane.fleet.usecase.fueling;

import com.octane.fleet.domain.FleetFueling;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.domain.repository.FleetFuelingRepository;
import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.fleet.usecase.FleetDriverResponse;
import com.octane.fleet.usecase.FleetFuelingResponse;
import com.octane.fleet.usecase.FleetVehicleResponse;
import com.octane.fleet.usecase.driver.CreateFleetDriverUseCase;
import com.octane.fleet.usecase.vehicle.CreateFleetVehicleUseCase;
import com.octane.fueling.usecase.fueling.RegisterFuelingRequest;
import com.octane.fueling.usecase.fueling.RegisterFuelingUseCase;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RegisterFleetFuelingUseCase {

    private final FleetDriverRepository fleetDriverRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final FleetClientRepository fleetClientRepository;
    private final FleetFuelingRepository fleetFuelingRepository;
    private final NozzleRepository nozzleRepository;
    private final RegisterFuelingUseCase registerFuelingUseCase;

    public RegisterFleetFuelingUseCase(FleetDriverRepository fleetDriverRepository,
                                       FleetVehicleRepository fleetVehicleRepository,
                                       FleetClientRepository fleetClientRepository,
                                       FleetFuelingRepository fleetFuelingRepository,
                                       NozzleRepository nozzleRepository,
                                       RegisterFuelingUseCase registerFuelingUseCase) {
        this.fleetDriverRepository = fleetDriverRepository;
        this.fleetVehicleRepository = fleetVehicleRepository;
        this.fleetClientRepository = fleetClientRepository;
        this.fleetFuelingRepository = fleetFuelingRepository;
        this.nozzleRepository = nozzleRepository;
        this.registerFuelingUseCase = registerFuelingUseCase;
    }

    @Transactional
    public FleetFuelingResponse execute(RegisterFleetFuelingRequest request) {
        var driverId = request.driverId();
        var driver = fleetDriverRepository.findById(driverId)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado: " + driverId));

        if (!driver.isActive()) {
            throw new BusinessException("Motorista inativo");
        }

        var vehicleId = request.vehicleId();
        var vehicle = fleetVehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new EntityNotFoundException("Veículo não encontrado: " + vehicleId));

        if (!vehicle.isActive()) {
            throw new BusinessException("Veículo inativo");
        }

        if (!vehicle.getClient().getId().equals(driver.getClient().getId())) {
            throw new BusinessException("Veículo não pertence ao cliente do motorista");
        }

        var nozzleId = request.nozzleId();
        var nozzle = nozzleRepository.findById(nozzleId)
                .orElseThrow(() -> new EntityNotFoundException("Bico não encontrado: " + nozzleId));

        if (!nozzle.getFuel().getId().equals(vehicle.getAllowedFuel().getId())) {
            throw new BusinessException("Combustível não permitido para este veículo");
        }

        // Check monthly limit before registering fueling (when totalAmount is informed)
        var client = vehicle.getClient();
        if (client.getMonthlyLimit() != null && request.totalAmount() != null) {
            var currentSpend = fleetClientRepository.sumCurrentMonthSpend(client.getId());
            if (currentSpend.add(request.totalAmount()).compareTo(client.getMonthlyLimit()) > 0) {
                throw new BusinessException("Limite mensal da frota seria ultrapassado");
            }
        }

        var fuelingRequest = new RegisterFuelingRequest(
                nozzleId,
                request.liters(),
                request.totalAmount(),
                request.paymentMethod(),
                vehicle.getPlate(),
                request.notes()
        );
        var fueling = registerFuelingUseCase.execute(request.shiftId(), fuelingRequest);

        // Post-fueling limit check when only liters were given (totalAmount was computed)
        if (client.getMonthlyLimit() != null && request.totalAmount() == null) {
            var currentSpend = fleetClientRepository.sumCurrentMonthSpend(client.getId());
            if (currentSpend.add(fueling.getTotalAmount()).compareTo(client.getMonthlyLimit()) > 0) {
                throw new BusinessException("Limite mensal da frota foi ultrapassado");
            }
        }

        var previousOdometer = fleetFuelingRepository.findLastOdometerByVehicleId(vehicleId).orElse(null);
        var odometerAlert = previousOdometer != null && request.odometer() < previousOdometer;

        var fleetFueling = new FleetFueling(null, fueling, driver, vehicle,
                request.odometer(), previousOdometer, odometerAlert, LocalDateTime.now());
        fleetFueling = fleetFuelingRepository.save(fleetFueling);

        FleetDriverResponse driverResponse = CreateFleetDriverUseCase.toResponse(driver);
        FleetVehicleResponse vehicleResponse = CreateFleetVehicleUseCase.toResponse(vehicle);

        return new FleetFuelingResponse(
                fleetFueling.getId(),
                fueling.getId(),
                driverResponse,
                vehicleResponse,
                fueling.getLiters(),
                fueling.getUnitPrice(),
                fueling.getTotalAmount(),
                fueling.getPaymentMethod().name(),
                fleetFueling.getOdometer(),
                fleetFueling.getPreviousOdometer(),
                fleetFueling.isOdometerAlert(),
                fueling.getFueledAt()
        );
    }
}
