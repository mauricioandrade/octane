package com.octane.fleet.usecase.client;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateFleetClientRequest(
        @Size(max = 150) String companyName,
        String tradeName,
        @Positive BigDecimal monthlyLimit,
        Boolean active
) {}
