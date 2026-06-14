package com.octane.fleet.handler;

import com.octane.fleet.usecase.FleetVehicleResponse;
import com.octane.fleet.usecase.vehicle.CreateFleetVehicleRequest;
import com.octane.fleet.usecase.vehicle.CreateFleetVehicleUseCase;
import com.octane.fleet.usecase.vehicle.FindFleetVehicleUseCase;
import com.octane.fleet.usecase.vehicle.ListFleetVehiclesUseCase;
import com.octane.fleet.usecase.vehicle.UpdateFleetVehicleRequest;
import com.octane.fleet.usecase.vehicle.UpdateFleetVehicleUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class FleetVehicleHandler {

    private final CreateFleetVehicleUseCase createFleetVehicleUseCase;
    private final UpdateFleetVehicleUseCase updateFleetVehicleUseCase;
    private final FindFleetVehicleUseCase findFleetVehicleUseCase;
    private final ListFleetVehiclesUseCase listFleetVehiclesUseCase;

    public FleetVehicleHandler(CreateFleetVehicleUseCase createFleetVehicleUseCase,
                               UpdateFleetVehicleUseCase updateFleetVehicleUseCase,
                               FindFleetVehicleUseCase findFleetVehicleUseCase,
                               ListFleetVehiclesUseCase listFleetVehiclesUseCase) {
        this.createFleetVehicleUseCase = createFleetVehicleUseCase;
        this.updateFleetVehicleUseCase = updateFleetVehicleUseCase;
        this.findFleetVehicleUseCase = findFleetVehicleUseCase;
        this.listFleetVehiclesUseCase = listFleetVehiclesUseCase;
    }

    @PostMapping("/api/fleet/vehicles")
    @ResponseStatus(HttpStatus.CREATED)
    public FleetVehicleResponse create(@Valid @RequestBody CreateFleetVehicleRequest request) {
        return createFleetVehicleUseCase.execute(request);
    }

    @GetMapping("/api/fleet/clients/{clientId}/vehicles")
    public List<FleetVehicleResponse> listByClient(@PathVariable UUID clientId,
                                                    @RequestParam(required = false) Boolean active) {
        return listFleetVehiclesUseCase.execute(clientId, active);
    }

    @GetMapping("/api/fleet/vehicles/{id}")
    public FleetVehicleResponse find(@PathVariable UUID id) {
        return findFleetVehicleUseCase.execute(id);
    }

    @PutMapping("/api/fleet/vehicles/{id}")
    public FleetVehicleResponse update(@PathVariable UUID id,
                                       @Valid @RequestBody UpdateFleetVehicleRequest request) {
        return updateFleetVehicleUseCase.execute(id, request);
    }

    @PatchMapping("/api/fleet/vehicles/{id}/status")
    public FleetVehicleResponse updateStatus(@PathVariable UUID id,
                                              @RequestBody Map<String, Boolean> body) {
        return updateFleetVehicleUseCase.execute(id, new UpdateFleetVehicleRequest(null, null, body.get("active")));
    }
}
