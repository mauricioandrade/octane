package com.octane.serviceorder.usecase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateServiceOrderRequest(
        @NotNull UUID stationId,
        @NotBlank @Size(max = 10) String plate,
        @NotNull @Positive Integer odometer,
        String customerName,
        String customerPhone,
        String notes
) {}
