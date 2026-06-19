package com.octane.station.handler;

import com.octane.station.usecase.pump.PumpResponse;
import com.octane.station.usecase.pump.CreatePumpRequest;
import com.octane.station.usecase.pump.CreatePumpUseCase;
import com.octane.station.usecase.pump.ListPumpsByStationUseCase;
import com.octane.station.usecase.station.StationResponse;
import com.octane.station.usecase.station.CreateStationRequest;
import com.octane.station.usecase.station.CreateStationUseCase;
import com.octane.station.usecase.station.FindStationUseCase;
import com.octane.station.usecase.station.ListStationsUseCase;
import com.octane.station.usecase.station.UpdateStationRequest;
import com.octane.station.usecase.station.UpdateStationStatusRequest;
import com.octane.station.usecase.station.UpdateStationStatusUseCase;
import com.octane.station.usecase.station.UpdateStationUseCase;
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
import java.util.UUID;

@RestController
@RequestMapping("/api/stations")
public class StationHandler {

    private final CreateStationUseCase createStationUseCase;
    private final FindStationUseCase findStationUseCase;
    private final ListStationsUseCase listStationsUseCase;
    private final CreatePumpUseCase createPumpUseCase;
    private final ListPumpsByStationUseCase listPumpsByStationUseCase;
    private final UpdateStationUseCase updateStationUseCase;
    private final UpdateStationStatusUseCase updateStationStatusUseCase;

    public StationHandler(
        CreateStationUseCase createStationUseCase,
        FindStationUseCase findStationUseCase,
        ListStationsUseCase listStationsUseCase,
        CreatePumpUseCase createPumpUseCase,
        ListPumpsByStationUseCase listPumpsByStationUseCase,
        UpdateStationUseCase updateStationUseCase,
        UpdateStationStatusUseCase updateStationStatusUseCase
    ) {
        this.createStationUseCase = createStationUseCase;
        this.findStationUseCase = findStationUseCase;
        this.listStationsUseCase = listStationsUseCase;
        this.createPumpUseCase = createPumpUseCase;
        this.listPumpsByStationUseCase = listPumpsByStationUseCase;
        this.updateStationUseCase = updateStationUseCase;
        this.updateStationStatusUseCase = updateStationStatusUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse create(@Valid @RequestBody CreateStationRequest request) {
        return StationResponse.from(createStationUseCase.execute(request));
    }

    @GetMapping
    public List<StationResponse> list(@RequestParam(required = false) Boolean active) {
        return listStationsUseCase.execute(active).stream()
            .map(StationResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public StationResponse findById(@PathVariable UUID id) {
        return StationResponse.from(findStationUseCase.execute(id));
    }

    @PutMapping("/{id}")
    public StationResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateStationRequest request) {
        return StationResponse.from(updateStationUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/status")
    public StationResponse updateStatus(@PathVariable UUID id, @RequestBody UpdateStationStatusRequest request) {
        return StationResponse.from(updateStationStatusUseCase.execute(id, request));
    }

    @PostMapping("/{id}/pumps")
    @ResponseStatus(HttpStatus.CREATED)
    public PumpResponse createPump(@PathVariable UUID id, @Valid @RequestBody CreatePumpRequest request) {
        return PumpResponse.from(createPumpUseCase.execute(id, request));
    }

    @GetMapping("/{id}/pumps")
    public List<PumpResponse> listPumps(@PathVariable UUID id,
                                        @RequestParam(required = false) String status) {
        return listPumpsByStationUseCase.execute(id, status).stream()
            .map(PumpResponse::from)
            .toList();
    }
}
