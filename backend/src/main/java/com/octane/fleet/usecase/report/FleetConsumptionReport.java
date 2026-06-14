package com.octane.fleet.usecase.report;

import java.util.List;

public record FleetConsumptionReport(
        FleetConsumptionSummary summary,
        List<FleetConsumptionLine> lines
) {}
