package com.octane.report.handler;

import com.octane.report.usecase.GetSalesReportUseCase;
import com.octane.report.usecase.GetShiftReportUseCase;
import com.octane.report.usecase.SalesReportResponse;
import com.octane.report.usecase.ShiftReportResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportHandler {

    private final GetSalesReportUseCase getSalesReportUseCase;
    private final GetShiftReportUseCase getShiftReportUseCase;

    public ReportHandler(GetSalesReportUseCase getSalesReportUseCase,
                         GetShiftReportUseCase getShiftReportUseCase) {
        this.getSalesReportUseCase = getSalesReportUseCase;
        this.getShiftReportUseCase = getShiftReportUseCase;
    }

    @GetMapping("/sales")
    public SalesReportResponse salesReport(
            @RequestParam UUID stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return getSalesReportUseCase.execute(stationId, from, to);
    }

    @GetMapping("/shifts")
    public ShiftReportResponse shiftReport(
            @RequestParam UUID stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return getShiftReportUseCase.execute(stationId, from, to);
    }
}
