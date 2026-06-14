package com.octane.serviceorder.usecase;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record AddServiceOrderItemRequest(
        @NotBlank @Size(max = 200) String description,
        @NotNull @Positive BigDecimal quantity,
        @NotNull @Positive BigDecimal unitPrice
) {}
