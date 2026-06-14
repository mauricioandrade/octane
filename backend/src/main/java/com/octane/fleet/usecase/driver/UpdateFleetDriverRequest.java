package com.octane.fleet.usecase.driver;

import jakarta.validation.constraints.Pattern;

public record UpdateFleetDriverRequest(
        String name,
        @Pattern(regexp = "\\d{6}") String pin,
        String rfidTag,
        Boolean active
) {}
