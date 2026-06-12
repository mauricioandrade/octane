package com.octane.pricing.usecase;

import java.math.BigDecimal;
import java.util.UUID;

public record SetFuelPriceRequest(UUID fuelId, BigDecimal price) {}
