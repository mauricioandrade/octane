package com.octane.fueling.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.fueling.domain.NozzleReading;
import com.octane.fueling.domain.NozzleReadingType;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.usecase.reading.RegisterNozzleReadingRequest;
import com.octane.fueling.usecase.reading.RegisterNozzleReadingUseCase;
import com.octane.fueling.usecase.shift.CloseShiftUseCase;
import com.octane.fueling.usecase.shift.FindShiftUseCase;
import com.octane.fueling.usecase.shift.ListShiftsByStationUseCase;
import com.octane.fueling.usecase.shift.OpenShiftRequest;
import com.octane.fueling.usecase.shift.OpenShiftUseCase;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ShiftHandler.class)
class ShiftHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private OpenShiftUseCase openShiftUseCase;

    @MockitoBean
    private CloseShiftUseCase closeShiftUseCase;

    @MockitoBean
    private FindShiftUseCase findShiftUseCase;

    @MockitoBean
    private ListShiftsByStationUseCase listShiftsByStationUseCase;

    @MockitoBean
    private RegisterNozzleReadingUseCase registerNozzleReadingUseCase;

    private Station buildStation(UUID id) {
        return new Station(id, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Shift buildShift(UUID id, Station station) {
        return new Shift(id, station, "Funcionario A", ShiftStatus.OPEN,
            LocalDateTime.now(), null, "Notas", LocalDateTime.now());
    }

    private NozzleReading buildNozzleReading(Shift shift) {
        var station = shift.getStation();
        var pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        var nozzle = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true, LocalDateTime.now(), LocalDateTime.now());
        return new NozzleReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.OPENING,
            new BigDecimal("1000.000"), LocalDateTime.now());
    }

    @Test
    void postShifts_returns201() throws Exception {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var station = buildStation(stationId);
        var shift = buildShift(shiftId, station);
        when(openShiftUseCase.execute(any(OpenShiftRequest.class))).thenReturn(shift);

        mockMvc.perform(post("/api/shifts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new OpenShiftRequest(stationId, "Funcionario A", "Notas"))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(shiftId.toString()))
            .andExpect(jsonPath("$.employeeName").value("Funcionario A"))
            .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void postClose_returns200() throws Exception {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var station = buildStation(stationId);
        var closedShift = new Shift(shiftId, station, "Funcionario A", ShiftStatus.CLOSED,
            LocalDateTime.now(), LocalDateTime.now(), "Notas", LocalDateTime.now());
        when(closeShiftUseCase.execute(shiftId)).thenReturn(closedShift);

        mockMvc.perform(post("/api/shifts/" + shiftId + "/close"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(shiftId.toString()))
            .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    void getShift_returns200() throws Exception {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var station = buildStation(stationId);
        var shift = buildShift(shiftId, station);
        when(findShiftUseCase.execute(shiftId)).thenReturn(shift);

        mockMvc.perform(get("/api/shifts/" + shiftId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(shiftId.toString()))
            .andExpect(jsonPath("$.stationId").value(stationId.toString()));
    }

    @Test
    void getShift_returns404_whenNotFound() throws Exception {
        var shiftId = UUID.randomUUID();
        when(findShiftUseCase.execute(shiftId))
            .thenThrow(new EntityNotFoundException("Shift not found: " + shiftId));

        mockMvc.perform(get("/api/shifts/" + shiftId))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message").value("Shift not found: " + shiftId));
    }

    @Test
    void listByStation_returns200() throws Exception {
        var stationId = UUID.randomUUID();
        var station = buildStation(stationId);
        var shift = buildShift(UUID.randomUUID(), station);
        when(listShiftsByStationUseCase.execute(stationId)).thenReturn(List.of(shift));

        mockMvc.perform(get("/api/stations/" + stationId + "/shifts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].employeeName").value("Funcionario A"))
            .andExpect(jsonPath("$[0].stationId").value(stationId.toString()));
    }

    @Test
    void postReading_returns201() throws Exception {
        var shiftId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();
        var stationId = UUID.randomUUID();
        var station = buildStation(stationId);
        var shift = buildShift(shiftId, station);
        var reading = buildNozzleReading(shift);
        when(registerNozzleReadingUseCase.execute(eq(shiftId), any(RegisterNozzleReadingRequest.class)))
            .thenReturn(reading);

        mockMvc.perform(post("/api/shifts/" + shiftId + "/readings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterNozzleReadingRequest(nozzleId, "OPENING", new BigDecimal("1000.000")))))
            .andExpect(status().isCreated());
    }
}
