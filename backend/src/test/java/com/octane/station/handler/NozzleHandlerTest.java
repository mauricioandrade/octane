package com.octane.station.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.usecase.nozzle.UpdateNozzleRequest;
import com.octane.station.usecase.nozzle.UpdateNozzleStatusRequest;
import com.octane.station.usecase.nozzle.UpdateNozzleStatusUseCase;
import com.octane.station.usecase.nozzle.UpdateNozzleUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NozzleHandler.class)
class NozzleHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private UpdateNozzleUseCase updateNozzleUseCase;

    @MockitoBean
    private UpdateNozzleStatusUseCase updateNozzleStatusUseCase;

    private Nozzle buildNozzle(UUID id, boolean active) {
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
            LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        return new Nozzle(id, 1, pump, fuel, active, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void putNozzle_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updateNozzleUseCase.execute(eq(id), any(UpdateNozzleRequest.class)))
            .thenReturn(buildNozzle(id, true));

        mockMvc.perform(put("/api/nozzles/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateNozzleRequest(1, UUID.randomUUID()))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void patchNozzleStatus_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updateNozzleStatusUseCase.execute(eq(id), any(UpdateNozzleStatusRequest.class)))
            .thenReturn(buildNozzle(id, false));

        mockMvc.perform(patch("/api/nozzles/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateNozzleStatusRequest(false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.active").value(false));
    }
}
