package com.octane.shared.auth;

import com.octane.user.domain.User;

import java.util.List;
import java.util.UUID;

public record AuthUserResponse(String username, String name, String role, List<UUID> stationIds) {

    public static AuthUserResponse from(User user, List<UUID> stationIds) {
        return new AuthUserResponse(user.getUsername(), user.getName(), user.getRole().name(), stationIds);
    }
}
