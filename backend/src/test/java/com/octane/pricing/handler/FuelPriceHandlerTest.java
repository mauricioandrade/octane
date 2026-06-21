package com.octane.pricing.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.usecase.GetCurrentPricesUseCase;
import com.octane.pricing.usecase.ListPriceHistoryUseCase;
import com.octane.pricing.usecase.SetFuelPriceRequest;
import com.octane.pricing.usecase.SetFuelPriceUseCase;
import com.octane.shared.auth.AuthenticatedUserService;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
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

@WebMvcTest(FuelPriceHandler.class)
class FuelPriceHandlerTest {

    @MockitoBean
    private AuthenticatedUserService authService;

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private SetFuelPriceUseCase setFuelPriceUseCase;

    @MockitoBean
    private GetCurrentPricesUseCase getCurrentPricesUseCase;

    @MockitoBean
    private ListPriceHistoryUseCase listPriceHistoryUseCase;

    private FuelPrice buildPrice(UUID stationId, UUID fuelId) {
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(fuelId, "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        return new FuelPrice(UUID.randomUUID(), station, fuel, new BigDecimal("5.8990"),
            LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void postPrice_returns201WithBody() throws Exception {
        var stationId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(setFuelPriceUseCase.execute(eq(stationId), any(SetFuelPriceRequest.class)))
            .thenReturn(buildPrice(stationId, fuelId));

        mockMvc.perform(post("/api/stations/" + stationId + "/prices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SetFuelPriceRequest(fuelId, new BigDecimal("5.8990")))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.price").value(5.8990))
            .andExpect(jsonPath("$.fuelName").value("Gasolina Comum"));
    }

    @Test
    void getPrices_returns200WithList() throws Exception {
        var stationId = UUID.randomUUID();
        when(getCurrentPricesUseCase.execute(stationId))
            .thenReturn(List.of(buildPrice(stationId, UUID.randomUUID())));

        mockMvc.perform(get("/api/stations/" + stationId + "/prices"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].fuelName").value("Gasolina Comum"));
    }

    @Test
    void getPriceHistory_returns200WithList() throws Exception {
        var stationId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(listPriceHistoryUseCase.execute(stationId, fuelId))
            .thenReturn(List.of(buildPrice(stationId, fuelId)));

        mockMvc.perform(get("/api/stations/" + stationId + "/prices/history?fuelId=" + fuelId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].price").value(5.8990));
    }
}
