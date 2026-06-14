package com.octane.commission.usecase.rule;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateCommissionRuleRequest(
        @Size(max = 100) String employeeName,
        @DecimalMin("0.0001") @DecimalMax("1.0000") BigDecimal rate,
        Boolean active
) {}
