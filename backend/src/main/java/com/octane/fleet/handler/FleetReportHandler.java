package com.octane.fleet.handler;

import com.octane.fleet.usecase.report.ExportFleetConsumptionCsvUseCase;
import com.octane.fleet.usecase.report.FleetConsumptionReport;
import com.octane.fleet.usecase.report.GetFleetConsumptionReportUseCase;
import com.octane.shared.auth.AuthenticatedUserService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/fleet/reports")
public class FleetReportHandler {

    private final GetFleetConsumptionReportUseCase getFleetConsumptionReportUseCase;
    private final ExportFleetConsumptionCsvUseCase exportFleetConsumptionCsvUseCase;
    private final AuthenticatedUserService authService;

    public FleetReportHandler(GetFleetConsumptionReportUseCase getFleetConsumptionReportUseCase,
                              ExportFleetConsumptionCsvUseCase exportFleetConsumptionCsvUseCase,
                              AuthenticatedUserService authService) {
        this.getFleetConsumptionReportUseCase = getFleetConsumptionReportUseCase;
        this.exportFleetConsumptionCsvUseCase = exportFleetConsumptionCsvUseCase;
        this.authService = authService;
    }

    @GetMapping("/consumption")
    public FleetConsumptionReport consumption(
            @RequestParam UUID stationId,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        authService.validateStationAccess(stationId);
        return getFleetConsumptionReportUseCase.execute(stationId, clientId, vehicleId, driverId, from, to);
    }

    @GetMapping("/consumption/csv")
    public ResponseEntity<byte[]> consumptionCsv(
            @RequestParam UUID stationId,
            @RequestParam(required = false) UUID clientId,
            @RequestParam(required = false) UUID vehicleId,
            @RequestParam(required = false) UUID driverId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        authService.validateStationAccess(stationId);
        var csv = exportFleetConsumptionCsvUseCase.execute(stationId, clientId, vehicleId, driverId, from, to);
        var filename = "frota_" + (clientId != null ? clientId : "all") + "_"
                + (from != null ? from : "inicio") + "_"
                + (to != null ? to : "fim") + ".csv";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + filename + "\"")
                .body(csv);
    }
}
