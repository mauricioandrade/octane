package com.octane.fleet.handler;

import com.octane.fleet.usecase.FleetDriverIdentificationResponse;
import com.octane.fleet.usecase.FleetDriverResponse;
import com.octane.fleet.usecase.driver.CreateFleetDriverRequest;
import com.octane.fleet.usecase.driver.CreateFleetDriverUseCase;
import com.octane.fleet.usecase.driver.FindFleetDriverUseCase;
import com.octane.fleet.usecase.driver.IdentifyFleetDriverRequest;
import com.octane.fleet.usecase.driver.IdentifyFleetDriverUseCase;
import com.octane.fleet.usecase.driver.ListFleetDriversUseCase;
import com.octane.fleet.usecase.driver.DeleteFleetDriverUseCase;
import com.octane.fleet.usecase.driver.UpdateFleetDriverRequest;
import com.octane.fleet.usecase.driver.UpdateFleetDriverUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class FleetDriverHandler {

    private final CreateFleetDriverUseCase createFleetDriverUseCase;
    private final UpdateFleetDriverUseCase updateFleetDriverUseCase;
    private final FindFleetDriverUseCase findFleetDriverUseCase;
    private final ListFleetDriversUseCase listFleetDriversUseCase;
    private final IdentifyFleetDriverUseCase identifyFleetDriverUseCase;
    private final DeleteFleetDriverUseCase deleteFleetDriverUseCase;

    public FleetDriverHandler(CreateFleetDriverUseCase createFleetDriverUseCase,
                              UpdateFleetDriverUseCase updateFleetDriverUseCase,
                              FindFleetDriverUseCase findFleetDriverUseCase,
                              ListFleetDriversUseCase listFleetDriversUseCase,
                              IdentifyFleetDriverUseCase identifyFleetDriverUseCase,
                              DeleteFleetDriverUseCase deleteFleetDriverUseCase) {
        this.createFleetDriverUseCase = createFleetDriverUseCase;
        this.updateFleetDriverUseCase = updateFleetDriverUseCase;
        this.findFleetDriverUseCase = findFleetDriverUseCase;
        this.listFleetDriversUseCase = listFleetDriversUseCase;
        this.identifyFleetDriverUseCase = identifyFleetDriverUseCase;
        this.deleteFleetDriverUseCase = deleteFleetDriverUseCase;
    }

    @PostMapping("/api/fleet/drivers")
    @ResponseStatus(HttpStatus.CREATED)
    public FleetDriverResponse create(@Valid @RequestBody CreateFleetDriverRequest request) {
        return createFleetDriverUseCase.execute(request);
    }

    @GetMapping("/api/fleet/clients/{clientId}/drivers")
    public List<FleetDriverResponse> listByClient(@PathVariable UUID clientId,
                                                   @RequestParam(required = false) Boolean active) {
        return listFleetDriversUseCase.execute(clientId, active);
    }

    @GetMapping("/api/fleet/drivers/{id}")
    public FleetDriverResponse find(@PathVariable UUID id) {
        return findFleetDriverUseCase.execute(id);
    }

    @PutMapping("/api/fleet/drivers/{id}")
    public FleetDriverResponse update(@PathVariable UUID id,
                                      @Valid @RequestBody UpdateFleetDriverRequest request) {
        return updateFleetDriverUseCase.execute(id, request);
    }

    @PatchMapping("/api/fleet/drivers/{id}/status")
    public FleetDriverResponse updateStatus(@PathVariable UUID id,
                                            @RequestBody Map<String, Boolean> body) {
        return updateFleetDriverUseCase.execute(id, new UpdateFleetDriverRequest(null, null, null, body.get("active")));
    }

    @PostMapping("/api/fleet/drivers/identify")
    public FleetDriverIdentificationResponse identify(@Valid @RequestBody IdentifyFleetDriverRequest request) {
        return identifyFleetDriverUseCase.execute(request);
    }

    @DeleteMapping("/api/fleet/drivers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteFleetDriverUseCase.execute(id);
    }
}
