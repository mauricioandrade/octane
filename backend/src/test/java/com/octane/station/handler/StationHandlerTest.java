package com.octane.station.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.usecase.pump.CreatePumpRequest;
import com.octane.station.usecase.pump.CreatePumpUseCase;
import com.octane.station.usecase.pump.ListPumpsByStationUseCase;
import com.octane.station.usecase.station.CreateStationRequest;
import com.octane.station.usecase.station.CreateStationUseCase;
import com.octane.station.usecase.station.FindStationUseCase;
import com.octane.station.usecase.station.ListStationsUseCase;
import com.octane.station.usecase.station.UpdateStationRequest;
import com.octane.station.usecase.station.UpdateStationStatusRequest;
import com.octane.station.usecase.station.UpdateStationStatusUseCase;
import com.octane.station.usecase.station.UpdateStationUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(StationHandler.class)
class StationHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CreateStationUseCase createStationUseCase;

    @MockitoBean
    private FindStationUseCase findStationUseCase;

    @MockitoBean
    private ListStationsUseCase listStationsUseCase;

    @MockitoBean
    private CreatePumpUseCase createPumpUseCase;

    @MockitoBean
    private ListPumpsByStationUseCase listPumpsByStationUseCase;

    @MockitoBean
    private UpdateStationUseCase updateStationUseCase;

    @MockitoBean
    private UpdateStationStatusUseCase updateStationStatusUseCase;

    private Station buildStation(UUID id) {
        return new Station(id, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Pump buildPump(UUID id, Station station) {
        return new Pump(id, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void postStations_returns201WithBody() throws Exception {
        var id = UUID.randomUUID();
        var station = buildStation(id);
        when(createStationUseCase.execute(any(CreateStationRequest.class))).thenReturn(station);

        mockMvc.perform(post("/api/stations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new CreateStationRequest("Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.cnpj").value("12.345.678/0001-90"));
    }

    @Test
    void getStations_returns200WithList() throws Exception {
        var station = buildStation(UUID.randomUUID());
        when(listStationsUseCase.execute((Boolean) null)).thenReturn(List.of(station));

        mockMvc.perform(get("/api/stations"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Posto X"));
    }

    @Test
    void getStationById_returns200_whenFound() throws Exception {
        var id = UUID.randomUUID();
        when(findStationUseCase.execute(id)).thenReturn(buildStation(id));

        mockMvc.perform(get("/api/stations/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void getStationById_returns404_whenNotFound() throws Exception {
        var id = UUID.randomUUID();
        when(findStationUseCase.execute(id)).thenThrow(new EntityNotFoundException("Station not found: " + id));

        mockMvc.perform(get("/api/stations/" + id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Station not found: " + id));
    }

    @Test
    void putStation_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updateStationUseCase.execute(eq(id), any(UpdateStationRequest.class)))
            .thenReturn(buildStation(id));

        mockMvc.perform(put("/api/stations/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new UpdateStationRequest("Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void patchStationStatus_returns200WithBody() throws Exception {
        var id = UUID.randomUUID();
        when(updateStationStatusUseCase.execute(eq(id), any(UpdateStationStatusRequest.class)))
            .thenReturn(buildStation(id));

        mockMvc.perform(patch("/api/stations/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UpdateStationStatusRequest(false))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void postPumps_returns201WithBody() throws Exception {
        var stationId = UUID.randomUUID();
        var station = buildStation(stationId);
        var pump = buildPump(UUID.randomUUID(), station);
        when(createPumpUseCase.execute(eq(stationId), any(CreatePumpRequest.class))).thenReturn(pump);

        mockMvc.perform(post("/api/stations/" + stationId + "/pumps")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreatePumpRequest(1))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.number").value(1))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void getPumps_returns200WithList() throws Exception {
        var stationId = UUID.randomUUID();
        var station = buildStation(stationId);
        when(listPumpsByStationUseCase.execute(eq(stationId), isNull())).thenReturn(List.of(buildPump(UUID.randomUUID(), station)));

        mockMvc.perform(get("/api/stations/" + stationId + "/pumps"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].number").value(1));
    }
}
