package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record FuelPriceResponse(
    UUID id,
    UUID fuelId,
    String fuelName,
    BigDecimal price,
    LocalDateTime effectiveFrom
) {
    public static FuelPriceResponse from(FuelPrice fuelPrice) {
        return new FuelPriceResponse(
            fuelPrice.getId(),
            fuelPrice.getFuel().getId(),
            fuelPrice.getFuel().getName(),
            fuelPrice.getPrice(),
            fuelPrice.getEffectiveFrom()
        );
    }
}
