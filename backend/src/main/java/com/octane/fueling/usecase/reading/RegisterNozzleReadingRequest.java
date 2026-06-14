package com.octane.fueling.usecase.reading;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.util.UUID;

public record RegisterNozzleReadingRequest(
        @NotNull UUID nozzleId,
        @NotBlank String type,
        @NotNull @PositiveOrZero BigDecimal totalizer
) {}
