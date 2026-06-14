package com.octane.fueling.usecase.shift;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record OpenShiftRequest(
        @NotNull UUID stationId,
        @NotBlank String employeeName,
        String notes
) {}
