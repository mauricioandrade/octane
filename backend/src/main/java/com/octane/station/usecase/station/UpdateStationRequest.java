package com.octane.station.usecase.station;

public record UpdateStationRequest(
    String name,
    String cnpj,
    String address,
    String city,
    String state
) {}
