package com.octane.fleet.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.fleet.usecase.fueling.FindFleetFuelingUseCase;
import com.octane.fleet.usecase.fueling.ListFleetFuelingsUseCase;
import com.octane.fleet.usecase.fueling.RegisterFleetFuelingRequest;
import com.octane.fleet.usecase.fueling.RegisterFleetFuelingUseCase;
import com.octane.shared.auth.AuthenticatedUserService;
import com.octane.shared.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FleetFuelingHandler.class)
class FleetFuelingHandlerTest {

    @MockitoBean
    private AuthenticatedUserService authService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterFleetFuelingUseCase registerFleetFuelingUseCase;

    @MockitoBean
    private FindFleetFuelingUseCase findFleetFuelingUseCase;

    @MockitoBean
    private ListFleetFuelingsUseCase listFleetFuelingsUseCase;

    @Test
    void postFleetFueling_returns422_whenFuelNotAllowed() throws Exception {
        when(registerFleetFuelingUseCase.execute(any(RegisterFleetFuelingRequest.class)))
                .thenThrow(new BusinessException("Combustível não permitido para este veículo"));

        var request = new RegisterFleetFuelingRequest(UUID.randomUUID(), UUID.randomUUID(),
                null, new BigDecimal("50.00"), "PIX",
                UUID.randomUUID(), UUID.randomUUID(), 11000, null);

        mockMvc.perform(post("/api/fleet/fuelings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Combustível não permitido para este veículo"));
    }

    @Test
    void postFleetFueling_returns400_whenRequiredFieldsMissing() throws Exception {
        var invalidRequest = "{}";

        mockMvc.perform(post("/api/fleet/fuelings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }
}
