package com.octane.fleet.usecase;

import java.time.LocalDateTime;
import java.util.UUID;

public record FleetDriverResponse(
        UUID id,
        UUID clientId,
        String name,
        String cpf,
        boolean hasPIN,
        boolean hasRFID,
        boolean active,
        LocalDateTime createdAt
) {}
