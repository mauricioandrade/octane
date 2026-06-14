package com.octane.station.usecase.fuel;

import jakarta.validation.constraints.NotBlank;

public record UpdateFuelRequest(@NotBlank String name, @NotBlank String unit) {}
