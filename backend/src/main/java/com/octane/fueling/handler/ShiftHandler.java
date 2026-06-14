package com.octane.fueling.handler;

import com.octane.fueling.usecase.reading.RegisterNozzleReadingRequest;
import com.octane.fueling.usecase.reading.RegisterNozzleReadingUseCase;
import com.octane.fueling.usecase.shift.CloseShiftUseCase;
import com.octane.fueling.usecase.shift.FindShiftUseCase;
import com.octane.fueling.usecase.shift.GetShiftReconciliationUseCase;
import com.octane.fueling.usecase.shift.ShiftReconciliationResponse;
import com.octane.fueling.usecase.shift.ListShiftsByStationUseCase;
import com.octane.fueling.usecase.shift.OpenShiftRequest;
import com.octane.fueling.usecase.shift.OpenShiftUseCase;
import com.octane.fueling.usecase.shift.ShiftResponse;
import com.octane.shared.pagination.PageResponse;
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

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
public class ShiftHandler {

    private final OpenShiftUseCase openShiftUseCase;
    private final CloseShiftUseCase closeShiftUseCase;
    private final FindShiftUseCase findShiftUseCase;
    private final ListShiftsByStationUseCase listShiftsByStationUseCase;
    private final RegisterNozzleReadingUseCase registerNozzleReadingUseCase;
    private final GetShiftReconciliationUseCase getShiftReconciliationUseCase;

    public ShiftHandler(
        OpenShiftUseCase openShiftUseCase,
        CloseShiftUseCase closeShiftUseCase,
        FindShiftUseCase findShiftUseCase,
        ListShiftsByStationUseCase listShiftsByStationUseCase,
        RegisterNozzleReadingUseCase registerNozzleReadingUseCase,
        GetShiftReconciliationUseCase getShiftReconciliationUseCase
    ) {
        this.openShiftUseCase = openShiftUseCase;
        this.closeShiftUseCase = closeShiftUseCase;
        this.findShiftUseCase = findShiftUseCase;
        this.listShiftsByStationUseCase = listShiftsByStationUseCase;
        this.registerNozzleReadingUseCase = registerNozzleReadingUseCase;
        this.getShiftReconciliationUseCase = getShiftReconciliationUseCase;
    }

    @GetMapping("/api/shifts/{id}/reconciliation")
    public ShiftReconciliationResponse reconciliation(@PathVariable UUID id) {
        return getShiftReconciliationUseCase.execute(id);
    }

    @PostMapping("/api/shifts")
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftResponse openShift(@Valid @RequestBody OpenShiftRequest request) {
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
    public PageResponse<ShiftResponse> listByStation(
        @PathVariable UUID stationId,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        return listShiftsByStationUseCase.execute(stationId, status, from, to, page, size)
            .map(ShiftResponse::from);
    }

    @PostMapping("/api/shifts/{id}/readings")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerReading(@PathVariable UUID id, @Valid @RequestBody RegisterNozzleReadingRequest request) {
        registerNozzleReadingUseCase.execute(id, request);
    }
}
