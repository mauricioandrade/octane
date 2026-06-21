package com.octane.user.usecase;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record UpdateUserStationsRequest(
        @NotNull Set<UUID> stationIds
) {}
