package com.octane.fleet.handler;

import com.octane.fleet.usecase.FleetClientResponse;
import com.octane.fleet.usecase.client.CreateFleetClientRequest;
import com.octane.fleet.usecase.client.CreateFleetClientUseCase;
import com.octane.fleet.usecase.client.FindFleetClientUseCase;
import com.octane.fleet.usecase.client.ListFleetClientsUseCase;
import com.octane.fleet.usecase.client.UpdateFleetClientRequest;
import com.octane.fleet.usecase.client.UpdateFleetClientUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/fleet/clients")
public class FleetClientHandler {

    private final CreateFleetClientUseCase createFleetClientUseCase;
    private final UpdateFleetClientUseCase updateFleetClientUseCase;
    private final FindFleetClientUseCase findFleetClientUseCase;
    private final ListFleetClientsUseCase listFleetClientsUseCase;

    public FleetClientHandler(CreateFleetClientUseCase createFleetClientUseCase,
                              UpdateFleetClientUseCase updateFleetClientUseCase,
                              FindFleetClientUseCase findFleetClientUseCase,
                              ListFleetClientsUseCase listFleetClientsUseCase) {
        this.createFleetClientUseCase = createFleetClientUseCase;
        this.updateFleetClientUseCase = updateFleetClientUseCase;
        this.findFleetClientUseCase = findFleetClientUseCase;
        this.listFleetClientsUseCase = listFleetClientsUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FleetClientResponse create(@Valid @RequestBody CreateFleetClientRequest request) {
        return createFleetClientUseCase.execute(request);
    }

    @GetMapping
    public List<FleetClientResponse> list(@RequestParam UUID stationId,
                                          @RequestParam(required = false) Boolean active) {
        return listFleetClientsUseCase.execute(stationId, active);
    }

    @GetMapping("/{id}")
    public FleetClientResponse find(@PathVariable UUID id) {
        return findFleetClientUseCase.execute(id);
    }

    @PutMapping("/{id}")
    public FleetClientResponse update(@PathVariable UUID id,
                                      @Valid @RequestBody UpdateFleetClientRequest request) {
        return updateFleetClientUseCase.execute(id, request);
    }

    @PatchMapping("/{id}/status")
    public FleetClientResponse updateStatus(@PathVariable UUID id,
                                            @RequestBody Map<String, Boolean> body) {
        var request = new UpdateFleetClientRequest(null, null, null, body.get("active"));
        return updateFleetClientUseCase.execute(id, request);
    }
}
