package com.octane.pricing.usecase;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record SetFuelPriceRequest(
        @NotNull UUID fuelId,
        @NotNull @Positive BigDecimal price
) {}
