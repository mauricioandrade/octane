package com.octane.fleet.usecase.driver;

import com.octane.fleet.domain.FleetDriver;
import com.octane.fleet.domain.IdentifierType;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.fleet.usecase.FleetClientResponse;
import com.octane.fleet.usecase.FleetDriverIdentificationResponse;
import com.octane.fleet.usecase.FleetVehicleResponse;
import com.octane.fleet.usecase.client.CreateFleetClientUseCase;
import com.octane.fleet.usecase.vehicle.CreateFleetVehicleUseCase;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class IdentifyFleetDriverUseCase {

    private final FleetDriverRepository fleetDriverRepository;
    private final FleetClientRepository fleetClientRepository;
    private final FleetVehicleRepository fleetVehicleRepository;
    private final PasswordEncoder passwordEncoder;

    public IdentifyFleetDriverUseCase(FleetDriverRepository fleetDriverRepository,
                                      FleetClientRepository fleetClientRepository,
                                      FleetVehicleRepository fleetVehicleRepository,
                                      PasswordEncoder passwordEncoder) {
        this.fleetDriverRepository = fleetDriverRepository;
        this.fleetClientRepository = fleetClientRepository;
        this.fleetVehicleRepository = fleetVehicleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public FleetDriverIdentificationResponse execute(IdentifyFleetDriverRequest request) {
        FleetDriver driver = switch (request.identifierType()) {
            case CPF -> findByCpf(request);
            case PIN -> findByPin(request);
            case RFID -> findByRfid(request);
        };

        if (!driver.isActive()) {
            throw new EntityNotFoundException("Motorista não encontrado");
        }

        var client = driver.getClient();
        var spend = fleetClientRepository.sumCurrentMonthSpend(client.getId());

        FleetClientResponse clientResponse = CreateFleetClientUseCase.toResponse(client, spend);
        var driverResponse = CreateFleetDriverUseCase.toResponse(driver);
        List<FleetVehicleResponse> vehicles = fleetVehicleRepository
                .findByClientId(client.getId(), true).stream()
                .map(CreateFleetVehicleUseCase::toResponse)
                .toList();

        return new FleetDriverIdentificationResponse(driverResponse, clientResponse, vehicles);
    }

    private FleetDriver findByCpf(IdentifyFleetDriverRequest request) {
        var drivers = fleetDriverRepository.findByCpfAndStationId(request.cpf(), request.stationId());
        return drivers.stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado"));
    }

    private FleetDriver findByPin(IdentifyFleetDriverRequest request) {
        if (request.pin() == null) {
            throw new EntityNotFoundException("Motorista não encontrado");
        }
        var drivers = fleetDriverRepository.findByCpfAndStationId(request.cpf(), request.stationId());
        return drivers.stream()
                .filter(d -> d.getPinHash() != null && passwordEncoder.matches(request.pin(), d.getPinHash()))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado"));
    }

    private FleetDriver findByRfid(IdentifyFleetDriverRequest request) {
        if (request.rfidTag() == null) {
            throw new EntityNotFoundException("Motorista não encontrado");
        }
        var drivers = fleetDriverRepository.findByRfidTagAndStationId(request.rfidTag(), request.stationId());
        return drivers.stream().findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado"));
    }
}
