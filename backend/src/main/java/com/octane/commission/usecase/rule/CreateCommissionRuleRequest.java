package com.octane.commission.usecase.rule;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateCommissionRuleRequest(
        @NotNull UUID stationId,
        @NotBlank @Size(max = 100) String employeeName,
        @NotNull @DecimalMin("0.0001") @DecimalMax("1.0000") BigDecimal rate
) {}
