package com.octane.fleet.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.fleet.domain.IdentifierType;
import com.octane.fleet.usecase.FleetClientResponse;
import com.octane.fleet.usecase.FleetDriverIdentificationResponse;
import com.octane.fleet.usecase.FleetDriverResponse;
import com.octane.fleet.usecase.driver.CreateFleetDriverRequest;
import com.octane.fleet.usecase.driver.CreateFleetDriverUseCase;
import com.octane.fleet.usecase.driver.FindFleetDriverUseCase;
import com.octane.fleet.usecase.driver.IdentifyFleetDriverRequest;
import com.octane.fleet.usecase.driver.IdentifyFleetDriverUseCase;
import com.octane.fleet.usecase.driver.ListFleetDriversUseCase;
import com.octane.fleet.usecase.driver.UpdateFleetDriverUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FleetDriverHandler.class)
class FleetDriverHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateFleetDriverUseCase createFleetDriverUseCase;

    @MockitoBean
    private UpdateFleetDriverUseCase updateFleetDriverUseCase;

    @MockitoBean
    private FindFleetDriverUseCase findFleetDriverUseCase;

    @MockitoBean
    private ListFleetDriversUseCase listFleetDriversUseCase;

    @MockitoBean
    private IdentifyFleetDriverUseCase identifyFleetDriverUseCase;

    @Test
    void postIdentify_returns200_withValidRequest() throws Exception {
        var driverResponse = new FleetDriverResponse(UUID.randomUUID(), UUID.randomUUID(),
                "João Silva", "123.456.789-09", true, false, true, LocalDateTime.now());
        var clientResponse = new FleetClientResponse(UUID.randomUUID(), UUID.randomUUID(),
                "12.345.678/0001-90", "Empresa LTDA", null, null, BigDecimal.ZERO, true, LocalDateTime.now());
        var identificationResponse = new FleetDriverIdentificationResponse(driverResponse, clientResponse, List.of());

        when(identifyFleetDriverUseCase.execute(any(IdentifyFleetDriverRequest.class)))
                .thenReturn(identificationResponse);

        var request = new IdentifyFleetDriverRequest(UUID.randomUUID(), "123.456.789-09",
                "123456", null, IdentifierType.PIN);

        mockMvc.perform(post("/api/fleet/drivers/identify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.driver.name").value("João Silva"));
    }

    @Test
    void postIdentify_returns400_withMissingBody() throws Exception {
        mockMvc.perform(post("/api/fleet/drivers/identify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postDriver_returns201_withValidRequest() throws Exception {
        var driverResponse = new FleetDriverResponse(UUID.randomUUID(), UUID.randomUUID(),
                "João Silva", "123.456.789-09", true, false, true, LocalDateTime.now());
        when(createFleetDriverUseCase.execute(any(CreateFleetDriverRequest.class))).thenReturn(driverResponse);

        var request = new CreateFleetDriverRequest(UUID.randomUUID(), "João Silva",
                "123.456.789-09", "123456", null);

        mockMvc.perform(post("/api/fleet/drivers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("João Silva"));
    }
}
