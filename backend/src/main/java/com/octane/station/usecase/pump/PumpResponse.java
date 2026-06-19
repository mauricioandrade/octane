package com.octane.station.usecase.pump;

import com.octane.station.domain.Pump;

import java.util.UUID;

public record PumpResponse(
    UUID id,
    int number,
    String status,
    UUID stationId
) {
    public static PumpResponse from(Pump pump) {
        return new PumpResponse(
            pump.getId(),
            pump.getNumber(),
            pump.getStatus().name(),
            pump.getStation().getId()
        );
    }
}
