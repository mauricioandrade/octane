package com.octane.station.usecase.station;

public record CreateStationRequest(
    String name,
    String cnpj,
    String address,
    String city,
    String state
) {}
