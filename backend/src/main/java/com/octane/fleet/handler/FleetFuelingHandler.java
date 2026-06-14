package com.octane.fleet.handler;

import com.octane.fleet.usecase.FleetFuelingResponse;
import com.octane.fleet.usecase.fueling.FindFleetFuelingUseCase;
import com.octane.fleet.usecase.fueling.ListFleetFuelingsUseCase;
import com.octane.fleet.usecase.fueling.RegisterFleetFuelingRequest;
import com.octane.fleet.usecase.fueling.RegisterFleetFuelingUseCase;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class FleetFuelingHandler {

    private final RegisterFleetFuelingUseCase registerFleetFuelingUseCase;
    private final FindFleetFuelingUseCase findFleetFuelingUseCase;
    private final ListFleetFuelingsUseCase listFleetFuelingsUseCase;

    public FleetFuelingHandler(RegisterFleetFuelingUseCase registerFleetFuelingUseCase,
                               FindFleetFuelingUseCase findFleetFuelingUseCase,
                               ListFleetFuelingsUseCase listFleetFuelingsUseCase) {
        this.registerFleetFuelingUseCase = registerFleetFuelingUseCase;
        this.findFleetFuelingUseCase = findFleetFuelingUseCase;
        this.listFleetFuelingsUseCase = listFleetFuelingsUseCase;
    }

    @PostMapping("/api/fleet/fuelings")
    @ResponseStatus(HttpStatus.CREATED)
    public FleetFuelingResponse register(@Valid @RequestBody RegisterFleetFuelingRequest request) {
        return registerFleetFuelingUseCase.execute(request);
    }

    @GetMapping("/api/fleet/fuelings/{id}")
    public FleetFuelingResponse find(@PathVariable UUID id) {
        return findFleetFuelingUseCase.execute(id);
    }

    @GetMapping("/api/fleet/clients/{clientId}/fuelings")
    public List<FleetFuelingResponse> listByClient(
            @PathVariable UUID clientId,
            @RequestParam UUID stationId,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return listFleetFuelingsUseCase.execute(stationId, clientId, vehicleId, driverId, from, to);
    }
}
