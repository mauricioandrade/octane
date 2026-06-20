package com.octane.shared.auth;

import com.octane.user.domain.User;

public record AuthUserResponse(String username, String name, String role) {

    public static AuthUserResponse from(User user) {
        return new AuthUserResponse(user.getUsername(), user.getName(), user.getRole().name());
    }
}
