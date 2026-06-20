package com.octane.user.usecase;

import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(max = 100) String name,
        String role,
        Boolean active,
        @Size(min = 6, max = 100) String password
) {}
