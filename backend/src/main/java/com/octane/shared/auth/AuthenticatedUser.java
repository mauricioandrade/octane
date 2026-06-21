package com.octane.shared.auth;

import com.octane.user.domain.UserRole;

import java.util.List;
import java.util.UUID;

public record AuthenticatedUser(
        UUID id,
        String username,
        UserRole role,
        List<UUID> stationIds
) {
    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }
}
