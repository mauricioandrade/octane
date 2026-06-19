package com.octane.station.usecase.nozzle;

import com.octane.station.domain.Nozzle;

import java.util.UUID;

public record NozzleResponse(
    UUID id,
    int number,
    UUID pumpId,
    UUID fuelId,
    boolean active
) {
    public static NozzleResponse from(Nozzle nozzle) {
        return new NozzleResponse(
            nozzle.getId(),
            nozzle.getNumber(),
            nozzle.getPump().getId(),
            nozzle.getFuel().getId(),
            nozzle.isActive()
        );
    }
}
