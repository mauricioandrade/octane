package com.octane.report.handler;

import com.octane.report.usecase.GetSalesReportUseCase;
import com.octane.report.usecase.GetShiftReportUseCase;
import com.octane.report.usecase.SalesReportResponse;
import com.octane.report.usecase.ShiftReportResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
public class ReportHandler {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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

    @GetMapping("/sales/csv")
    public ResponseEntity<byte[]> salesReportCsv(
            @RequestParam UUID stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var report = getSalesReportUseCase.execute(stationId, from, to);
        var sb = new StringBuilder();
        sb.append("Data;Receita;Litros;Quantidade\n");
        for (var d : report.daily()) {
            sb.append(d.date().format(DATE_FMT)).append(';')
              .append(d.revenue()).append(';')
              .append(d.liters()).append(';')
              .append(d.count()).append('\n');
        }
        return csvResponse(sb.toString(), "vendas.csv");
    }

    @GetMapping("/shifts/csv")
    public ResponseEntity<byte[]> shiftReportCsv(
            @RequestParam UUID stationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        var report = getShiftReportUseCase.execute(stationId, from, to);
        var sb = new StringBuilder();
        sb.append("Frentista;Abertura;Fechamento;Duração (min);Receita;Litros;Abastecimentos\n");
        for (var s : report.shifts()) {
            sb.append(s.employeeName()).append(';')
              .append(s.openedAt().format(DATETIME_FMT)).append(';')
              .append(s.closedAt().format(DATETIME_FMT)).append(';')
              .append(s.durationMinutes()).append(';')
              .append(s.revenue()).append(';')
              .append(s.liters()).append(';')
              .append(s.fuelingCount()).append('\n');
        }
        return csvResponse(sb.toString(), "turnos.csv");
    }

    private ResponseEntity<byte[]> csvResponse(String csv, String filename) {
        byte[] bom = {(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
        byte[] csvBytes = csv.getBytes(StandardCharsets.UTF_8);
        byte[] body = new byte[bom.length + csvBytes.length];
        System.arraycopy(bom, 0, body, 0, bom.length);
        System.arraycopy(csvBytes, 0, body, bom.length, csvBytes.length);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(body);
    }
}
