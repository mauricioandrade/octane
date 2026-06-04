package com.octane.fueling.usecase.shift;

import java.util.UUID;

public record OpenShiftRequest(UUID stationId, String employeeName, String notes) {}
