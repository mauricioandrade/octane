package com.octane.fueling.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.usecase.fueling.FuelingResponse;
import com.octane.fueling.usecase.fueling.ListFuelingsByShiftUseCase;
import com.octane.fueling.usecase.fueling.RegisterFuelingRequest;
import com.octane.fueling.usecase.fueling.RegisterFuelingUseCase;
import com.octane.fueling.usecase.fueling.ShiftSummaryResponse;
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

@WebMvcTest(FuelingHandler.class)
class FuelingHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private RegisterFuelingUseCase registerFuelingUseCase;

    @MockitoBean
    private ListFuelingsByShiftUseCase listFuelingsByShiftUseCase;

    private Station buildStation() {
        return new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Shift buildShift(Station station) {
        return new Shift(UUID.randomUUID(), station, "Funcionario A", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
    }

    private Nozzle buildNozzle(Station station) {
        var pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        return new Nozzle(UUID.randomUUID(), 1, pump, fuel, true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Fueling buildFueling(Shift shift, Nozzle nozzle) {
        return new Fueling(
            UUID.randomUUID(), shift, nozzle,
            new BigDecimal("10.000"),
            new BigDecimal("5.9990"),
            new BigDecimal("59.99"),
            PaymentMethod.PIX,
            FuelingStatus.ACTIVE,
            null,
            "ABC1234",
            null,
            LocalDateTime.now(),
            LocalDateTime.now()
        );
    }

    @Test
    void postFueling_returns201() throws Exception {
        var station = buildStation();
        var shift = buildShift(station);
        var nozzle = buildNozzle(station);
        var fueling = buildFueling(shift, nozzle);
        var shiftId = shift.getId();

        when(registerFuelingUseCase.execute(eq(shiftId), any(RegisterFuelingRequest.class))).thenReturn(fueling);

        mockMvc.perform(post("/api/shifts/" + shiftId + "/fuelings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
                        new BigDecimal("59.99"), "PIX", "ABC1234", null))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(fueling.getId().toString()))
            .andExpect(jsonPath("$.paymentMethod").value("PIX"))
            .andExpect(jsonPath("$.vehiclePlate").value("ABC1234"));
    }

    @Test
    void getFuelings_returns200() throws Exception {
        var station = buildStation();
        var shift = buildShift(station);
        var nozzle = buildNozzle(station);
        var fueling = buildFueling(shift, nozzle);
        var shiftId = shift.getId();

        var summary = new ShiftSummaryResponse(
            shiftId,
            List.of(FuelingResponse.from(fueling)),
            new BigDecimal("10.000"),
            new BigDecimal("59.99")
        );
        when(listFuelingsByShiftUseCase.execute(shiftId)).thenReturn(summary);

        mockMvc.perform(get("/api/shifts/" + shiftId + "/fuelings"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.shiftId").value(shiftId.toString()))
            .andExpect(jsonPath("$.fuelings[0].paymentMethod").value("PIX"))
            .andExpect(jsonPath("$.totalLiters").value(10.0));
    }
}
