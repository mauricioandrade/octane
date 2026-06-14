package com.octane.station.usecase.station;

import jakarta.validation.constraints.NotBlank;

public record CreateStationRequest(
    @NotBlank String name,
    @NotBlank String cnpj,
    String address,
    String city,
    String state
) {}
