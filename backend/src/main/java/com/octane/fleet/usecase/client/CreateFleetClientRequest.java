package com.octane.fleet.usecase.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateFleetClientRequest(
        @NotNull UUID stationId,
        @NotBlank @Pattern(regexp = "\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2}") String cnpj,
        @NotBlank @Size(max = 150) String companyName,
        String tradeName,
        @Positive BigDecimal monthlyLimit
) {}
