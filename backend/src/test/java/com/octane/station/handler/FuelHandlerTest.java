package com.octane.station.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.usecase.fuel.CreateFuelRequest;
import com.octane.station.usecase.fuel.CreateFuelUseCase;
import com.octane.station.usecase.fuel.ListFuelsUseCase;
import com.octane.station.usecase.fuel.UpdateFuelRequest;
import com.octane.station.usecase.fuel.UpdateFuelStatusRequest;
import com.octane.station.usecase.fuel.DeleteFuelUseCase;
import com.octane.station.usecase.fuel.UpdateFuelStatusUseCase;
import com.octane.station.usecase.fuel.UpdateFuelUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import com.octane.audit.usecase.AuditService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FuelHandler.class)
class FuelHandlerTest {

    @MockitoBean
    private AuditService auditService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ListFuelsUseCase listFuelsUseCase;

    @MockitoBean
    private UpdateFuelStatusUseCase updateFuelStatusUseCase;

    @MockitoBean
    private CreateFuelUseCase createFuelUseCase;

    @MockitoBean
    private UpdateFuelUseCase updateFuelUseCase;

    @MockitoBean
    private DeleteFuelUseCase deleteFuelUseCase;

    @Test
    void getFuels_returns200WithList() throws Exception {
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        when(listFuelsUseCase.execute()).thenReturn(List.of(fuel));

        mockMvc.perform(get("/api/fuels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Gasolina Comum"))
            .andExpect(jsonPath("$[0].unit").value("LITER"));
    }

    @Test
    void patchFuelStatus_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        var fuel = new Fuel(id, "Diesel S500", FuelUnit.LITER, false, LocalDateTime.now());
        when(updateFuelStatusUseCase.execute(eq(id), any(UpdateFuelStatusRequest.class))).thenReturn(fuel);

        mockMvc.perform(patch("/api/fuels/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateFuelStatusRequest(false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }
}
