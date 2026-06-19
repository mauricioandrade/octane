package com.octane.fleet.usecase.vehicle;

import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.fleet.usecase.FleetVehicleResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListFleetVehiclesUseCase {

    private final FleetVehicleRepository fleetVehicleRepository;

    public ListFleetVehiclesUseCase(FleetVehicleRepository fleetVehicleRepository) {
        this.fleetVehicleRepository = fleetVehicleRepository;
    }

    public List<FleetVehicleResponse> execute(UUID clientId, Boolean active) {
        return fleetVehicleRepository.findByClientId(clientId, active).stream()
                .map(FleetVehicleResponse::from)
                .toList();
    }
}
