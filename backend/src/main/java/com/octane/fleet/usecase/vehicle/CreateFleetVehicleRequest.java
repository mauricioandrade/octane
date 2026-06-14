package com.octane.fleet.usecase.vehicle;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateFleetVehicleRequest(
        @NotNull UUID clientId,
        @NotBlank @Pattern(regexp = "[A-Z]{3}[0-9]{4}|[A-Z]{3}[0-9][A-Z][0-9]{2}") String plate,
        String model,
        @NotNull UUID allowedFuelId
) {}
