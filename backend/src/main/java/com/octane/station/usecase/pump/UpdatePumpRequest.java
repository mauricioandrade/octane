package com.octane.station.usecase.pump;

import jakarta.validation.constraints.Positive;

public record UpdatePumpRequest(@Positive int number) {}
