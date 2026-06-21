package com.octane.fleet.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.fleet.usecase.FleetClientResponse;
import com.octane.fleet.usecase.client.CreateFleetClientRequest;
import com.octane.fleet.usecase.client.CreateFleetClientUseCase;
import com.octane.fleet.usecase.client.FindFleetClientUseCase;
import com.octane.fleet.usecase.client.ListFleetClientsUseCase;
import com.octane.fleet.usecase.client.DeleteFleetClientUseCase;
import com.octane.fleet.usecase.client.UpdateFleetClientRequest;
import com.octane.fleet.usecase.client.UpdateFleetClientUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.octane.audit.usecase.AuditService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FleetClientHandler.class)
class FleetClientHandlerTest {

    @MockitoBean
    private AuditService auditService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateFleetClientUseCase createFleetClientUseCase;

    @MockitoBean
    private UpdateFleetClientUseCase updateFleetClientUseCase;

    @MockitoBean
    private FindFleetClientUseCase findFleetClientUseCase;

    @MockitoBean
    private ListFleetClientsUseCase listFleetClientsUseCase;

    @MockitoBean
    private DeleteFleetClientUseCase deleteFleetClientUseCase;

    private FleetClientResponse buildResponse() {
        return new FleetClientResponse(UUID.randomUUID(), UUID.randomUUID(),
                "12.345.678/0001-90", "Empresa LTDA", "Fantasia",
                new BigDecimal("5000.00"), BigDecimal.ZERO, true, LocalDateTime.now());
    }

    @Test
    void postClient_returns201_withValidRequest() throws Exception {
        var response = buildResponse();
        when(createFleetClientUseCase.execute(any(CreateFleetClientRequest.class))).thenReturn(response);

        var request = new CreateFleetClientRequest(UUID.randomUUID(), "12.345.678/0001-90",
                "Empresa LTDA", "Fantasia", new BigDecimal("5000.00"));

        mockMvc.perform(post("/api/fleet/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-90"))
                .andExpect(jsonPath("$.companyName").value("Empresa LTDA"));
    }

    @Test
    void postClient_returns400_withInvalidCnpj() throws Exception {
        var request = new CreateFleetClientRequest(UUID.randomUUID(), "CNPJ-INVALIDO",
                "Empresa LTDA", null, null);

        mockMvc.perform(post("/api/fleet/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getClients_returns200_withList() throws Exception {
        var stationId = UUID.randomUUID();
        var response = buildResponse();
        when(listFleetClientsUseCase.execute(stationId, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/fleet/clients").param("stationId", stationId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyName").value("Empresa LTDA"));
    }
}
