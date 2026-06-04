package com.octane.fueling.handler;

import com.octane.fueling.usecase.reading.RegisterNozzleReadingRequest;
import com.octane.fueling.usecase.reading.RegisterNozzleReadingUseCase;
import com.octane.fueling.usecase.shift.CloseShiftUseCase;
import com.octane.fueling.usecase.shift.FindShiftUseCase;
import com.octane.fueling.usecase.shift.ListShiftsByStationUseCase;
import com.octane.fueling.usecase.shift.OpenShiftRequest;
import com.octane.fueling.usecase.shift.OpenShiftUseCase;
import com.octane.fueling.usecase.shift.ShiftResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class ShiftHandler {

    private final OpenShiftUseCase openShiftUseCase;
    private final CloseShiftUseCase closeShiftUseCase;
    private final FindShiftUseCase findShiftUseCase;
    private final ListShiftsByStationUseCase listShiftsByStationUseCase;
    private final RegisterNozzleReadingUseCase registerNozzleReadingUseCase;

    public ShiftHandler(
        OpenShiftUseCase openShiftUseCase,
        CloseShiftUseCase closeShiftUseCase,
        FindShiftUseCase findShiftUseCase,
        ListShiftsByStationUseCase listShiftsByStationUseCase,
        RegisterNozzleReadingUseCase registerNozzleReadingUseCase
    ) {
        this.openShiftUseCase = openShiftUseCase;
        this.closeShiftUseCase = closeShiftUseCase;
        this.findShiftUseCase = findShiftUseCase;
        this.listShiftsByStationUseCase = listShiftsByStationUseCase;
        this.registerNozzleReadingUseCase = registerNozzleReadingUseCase;
    }

    @PostMapping("/api/shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponse openShift(@RequestBody OpenShiftRequest request) {
        return ShiftResponse.from(openShiftUseCase.execute(request));
    }

    @PostMapping("/api/shifts/{id}/close")
    public ShiftResponse closeShift(@PathVariable UUID id) {
        return ShiftResponse.from(closeShiftUseCase.execute(id));
    }

    @GetMapping("/api/shifts/{id}")
    public ShiftResponse findShift(@PathVariable UUID id) {
        return ShiftResponse.from(findShiftUseCase.execute(id));
    }

    @GetMapping("/api/stations/{stationId}/shifts")
    public List<ShiftResponse> listByStation(@PathVariable UUID stationId) {
        return listShiftsByStationUseCase.execute(stationId).stream()
            .map(ShiftResponse::from)
            .toList();
    }

    @PostMapping("/api/shifts/{id}/readings")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerReading(@PathVariable UUID id, @RequestBody RegisterNozzleReadingRequest request) {
        registerNozzleReadingUseCase.execute(id, request);
    }
}
