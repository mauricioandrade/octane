package com.octane.station.usecase.fuel;

import jakarta.validation.constraints.NotBlank;

public record CreateFuelRequest(@NotBlank String name, @NotBlank String unit) {}
