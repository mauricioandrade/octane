package com.octane.fueling.usecase.reading;

import java.math.BigDecimal;
import java.util.UUID;

public record RegisterNozzleReadingRequest(UUID nozzleId, String type, BigDecimal totalizer) {}
