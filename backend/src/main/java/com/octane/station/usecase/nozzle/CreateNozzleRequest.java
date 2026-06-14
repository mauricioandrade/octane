package com.octane.station.usecase.nozzle;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record CreateNozzleRequest(@Positive int number, @NotNull UUID fuelId) {}
