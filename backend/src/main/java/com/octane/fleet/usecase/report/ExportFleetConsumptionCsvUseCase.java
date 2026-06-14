package com.octane.fleet.usecase.report;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class ExportFleetConsumptionCsvUseCase {

    private static final String HEADER =
            "Data/Hora,Cliente,CNPJ,Motorista,CPF,Veículo,Placa,Combustível,Litros,Valor R$,Hodômetro,Alerta Hodômetro,Forma de Pagamento";
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private final GetFleetConsumptionReportUseCase reportUseCase;

    public ExportFleetConsumptionCsvUseCase(GetFleetConsumptionReportUseCase reportUseCase) {
        this.reportUseCase = reportUseCase;
    }

    public byte[] execute(UUID stationId, UUID clientId, UUID vehicleId,
                          UUID driverId, LocalDate from, LocalDate to) {
        var report = reportUseCase.execute(stationId, clientId, vehicleId, driverId, from, to);

        var sb = new StringBuilder();
        sb.append(HEADER).append("\n");

        for (var line : report.lines()) {
            sb.append(format(line.fueledAt() != null ? line.fueledAt().format(DATETIME_FMT) : "")).append(",");
            sb.append(escape(line.clientName())).append(",");
            sb.append(escape(line.clientCnpj())).append(",");
            sb.append(escape(line.driverName())).append(",");
            sb.append(escape(line.driverCpf())).append(",");
            sb.append(escape(line.vehicleModel() != null ? line.vehicleModel() : "")).append(",");
            sb.append(escape(line.vehiclePlate())).append(",");
            sb.append(escape(line.fuelName())).append(",");
            sb.append(line.liters()).append(",");
            sb.append(line.totalAmount()).append(",");
            sb.append(line.odometer()).append(",");
            sb.append(line.odometerAlert() ? "Sim" : "Não").append(",");
            sb.append(escape(line.paymentMethod())).append("\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String format(String value) {
        return value != null ? value : "";
    }

    private String escape(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
