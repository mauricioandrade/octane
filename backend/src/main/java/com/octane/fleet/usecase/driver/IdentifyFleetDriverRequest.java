package com.octane.fleet.usecase.driver;

import com.octane.fleet.domain.IdentifierType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record IdentifyFleetDriverRequest(
        @NotNull UUID stationId,
        String cpf,
        String pin,
        String rfidTag,
        @NotNull IdentifierType identifierType
) {}
