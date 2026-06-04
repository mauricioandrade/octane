package com.octane.station.handler;

import com.octane.station.usecase.pump.CreatePumpRequest;
import com.octane.station.usecase.pump.CreatePumpUseCase;
import com.octane.station.usecase.pump.ListPumpsByStationUseCase;
import com.octane.station.usecase.station.CreateStationRequest;
import com.octane.station.usecase.station.CreateStationUseCase;
import com.octane.station.usecase.station.FindStationUseCase;
import com.octane.station.usecase.station.ListStationsUseCase;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    public StationHandler(
        CreateStationUseCase createStationUseCase,
        FindStationUseCase findStationUseCase,
        ListStationsUseCase listStationsUseCase,
        CreatePumpUseCase createPumpUseCase,
        ListPumpsByStationUseCase listPumpsByStationUseCase
    ) {
        this.createStationUseCase = createStationUseCase;
        this.findStationUseCase = findStationUseCase;
        this.listStationsUseCase = listStationsUseCase;
        this.createPumpUseCase = createPumpUseCase;
        this.listPumpsByStationUseCase = listPumpsByStationUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StationResponse create(@RequestBody CreateStationRequest request) {
        return StationResponse.from(createStationUseCase.execute(request));
    }

    @GetMapping
    public List<StationResponse> list() {
        return listStationsUseCase.execute().stream()
            .map(StationResponse::from)
            .toList();
    }

    @GetMapping("/{id}")
    public StationResponse findById(@PathVariable UUID id) {
        return StationResponse.from(findStationUseCase.execute(id));
    }

    @PostMapping("/{id}/pumps")
    @ResponseStatus(HttpStatus.CREATED)
    public PumpResponse createPump(@PathVariable UUID id, @RequestBody CreatePumpRequest request) {
        return PumpResponse.from(createPumpUseCase.execute(id, request));
    }

    @GetMapping("/{id}/pumps")
    public List<PumpResponse> listPumps(@PathVariable UUID id) {
        return listPumpsByStationUseCase.execute(id).stream()
            .map(PumpResponse::from)
            .toList();
    }
}
