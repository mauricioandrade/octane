package com.octane.station.usecase.station;

import com.octane.station.domain.Station;

import java.util.UUID;

public record StationResponse(
    UUID id,
    String name,
    String cnpj,
    String address,
    String city,
    String state,
    boolean active
) {
    public static StationResponse from(Station station) {
        return new StationResponse(
            station.getId(),
            station.getName(),
            station.getCnpj(),
            station.getAddress(),
            station.getCity(),
            station.getState(),
            station.isActive()
        );
    }
}
