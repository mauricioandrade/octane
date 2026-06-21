package com.octane.station.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.usecase.nozzle.CreateNozzleRequest;
import com.octane.station.usecase.nozzle.CreateNozzleUseCase;
import com.octane.station.usecase.nozzle.ListNozzlesByPumpUseCase;
import com.octane.station.usecase.pump.UpdatePumpRequest;
import com.octane.station.usecase.pump.UpdatePumpStatusRequest;
import com.octane.station.usecase.pump.DeletePumpUseCase;
import com.octane.station.usecase.pump.UpdatePumpStatusUseCase;
import com.octane.station.usecase.pump.UpdatePumpUseCase;
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
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PumpHandler.class)
class PumpHandlerTest {

    @MockitoBean
    private AuditService auditService;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateNozzleUseCase createNozzleUseCase;

    @MockitoBean
    private ListNozzlesByPumpUseCase listNozzlesByPumpUseCase;

    @MockitoBean
    private UpdatePumpUseCase updatePumpUseCase;

    @MockitoBean
    private UpdatePumpStatusUseCase updatePumpStatusUseCase;

    @MockitoBean
    private DeletePumpUseCase deletePumpUseCase;

    private Pump buildPump(UUID id, int number, PumpStatus status) {
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        return new Pump(id, number, status, station, LocalDateTime.now(), LocalDateTime.now());
    }

    private Nozzle buildNozzle(UUID pumpId, UUID fuelId) {
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(pumpId, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(fuelId, "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        return new Nozzle(UUID.randomUUID(), 1, pump, fuel, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void postNozzles_returns201WithBody() throws Exception {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        var nozzle = buildNozzle(pumpId, fuelId);
        when(createNozzleUseCase.execute(eq(pumpId), any(CreateNozzleRequest.class))).thenReturn(nozzle);

        mockMvc.perform(post("/api/pumps/" + pumpId + "/nozzles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CreateNozzleRequest(1, fuelId))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getNozzles_returns200WithList() throws Exception {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(listNozzlesByPumpUseCase.execute(eq(pumpId), isNull())).thenReturn(List.of(buildNozzle(pumpId, fuelId)));

        mockMvc.perform(get("/api/pumps/" + pumpId + "/nozzles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].number").value(1));
    }

    @Test
    void putPump_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updatePumpUseCase.execute(eq(id), any(UpdatePumpRequest.class)))
            .thenReturn(buildPump(id, 2, PumpStatus.ACTIVE));

        mockMvc.perform(put("/api/pumps/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new UpdatePumpRequest(2))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.number").value(2));
    }

    @Test
    void patchPumpStatus_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updatePumpStatusUseCase.execute(eq(id), any(UpdatePumpStatusRequest.class)))
            .thenReturn(buildPump(id, 1, PumpStatus.MAINTENANCE));

        mockMvc.perform(patch("/api/pumps/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new UpdatePumpStatusRequest("MAINTENANCE"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("MAINTENANCE"));
    }

    @Test
    void postNozzles_returns404_whenPumpNotFound() throws Exception {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(createNozzleUseCase.execute(eq(pumpId), any(CreateNozzleRequest.class)))
            .thenThrow(new com.octane.shared.exception.EntityNotFoundException("Bomba não encontrada: " + pumpId));

        mockMvc.perform(post("/api/pumps/" + pumpId + "/nozzles")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CreateNozzleRequest(1, fuelId))))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Bomba não encontrada: " + pumpId));
    }

    @Test
    void postNozzles_returns422_whenNozzleNumberAlreadyUsed() throws Exception {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(createNozzleUseCase.execute(eq(pumpId), any(CreateNozzleRequest.class)))
            .thenThrow(new com.octane.shared.exception.BusinessException("Bico número 1 já existe nesta bomba"));

        mockMvc.perform(post("/api/pumps/" + pumpId + "/nozzles")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(new ObjectMapper().writeValueAsString(new CreateNozzleRequest(1, fuelId))))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.message").value("Bico número 1 já existe nesta bomba"));
    }
}
