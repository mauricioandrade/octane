package com.octane.fleet.usecase.driver;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record CreateFleetDriverRequest(
        @NotNull UUID clientId,
        @NotBlank String name,
        @NotBlank @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}") String cpf,
        @Pattern(regexp = "\\d{6}") String pin,
        String rfidTag
) {}
