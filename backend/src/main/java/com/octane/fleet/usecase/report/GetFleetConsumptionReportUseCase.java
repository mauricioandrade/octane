package com.octane.fleet.usecase.report;

import com.octane.fleet.domain.FleetFueling;
import com.octane.fleet.domain.repository.FleetFuelingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class GetFleetConsumptionReportUseCase {

    private final FleetFuelingRepository fleetFuelingRepository;

    public GetFleetConsumptionReportUseCase(FleetFuelingRepository fleetFuelingRepository) {
        this.fleetFuelingRepository = fleetFuelingRepository;
    }

    public FleetConsumptionReport execute(UUID stationId, UUID clientId, UUID vehicleId,
                                          UUID driverId, LocalDate from, LocalDate to) {
        var fuelings = fleetFuelingRepository.findAllByFilters(stationId, clientId, vehicleId, driverId, from, to);

        var lines = fuelings.stream().map(this::toLine).toList();

        var totalLiters = lines.stream()
                .map(FleetConsumptionLine::liters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalAmount = lines.stream()
                .map(FleetConsumptionLine::totalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var odometerAlerts = (int) lines.stream().filter(FleetConsumptionLine::odometerAlert).count();

        var summary = new FleetConsumptionSummary(totalLiters, totalAmount, lines.size(), odometerAlerts);
        return new FleetConsumptionReport(summary, lines);
    }

    private FleetConsumptionLine toLine(FleetFueling ff) {
        var fueling = ff.getFueling();
        var driver = ff.getDriver();
        var vehicle = ff.getVehicle();
        var client = vehicle.getClient();
        return new FleetConsumptionLine(
                fueling.getFueledAt(),
                client.getCompanyName(),
                client.getCnpj(),
                driver.getName(),
                driver.getCpf(),
                vehicle.getPlate(),
                vehicle.getModel(),
                vehicle.getAllowedFuel().getName(),
                fueling.getLiters(),
                fueling.getTotalAmount(),
                ff.getOdometer(),
                ff.isOdometerAlert(),
                fueling.getPaymentMethod().name()
        );
    }
}
