package com.octane.station.usecase.fuel;

import com.octane.station.domain.Fuel;

import java.util.UUID;

public record FuelResponse(
    UUID id,
    String name,
    String unit,
    boolean active
) {
    public static FuelResponse from(Fuel fuel) {
        return new FuelResponse(
            fuel.getId(),
            fuel.getName(),
            fuel.getUnit().name(),
            fuel.isActive()
        );
    }
}
