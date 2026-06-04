package com.octane.station.handler;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.usecase.fuel.ListFuelsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FuelHandler.class)
class FuelHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ListFuelsUseCase listFuelsUseCase;

    @Test
    void getFuels_returns200WithList() throws Exception {
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        when(listFuelsUseCase.execute()).thenReturn(List.of(fuel));

        mockMvc.perform(get("/api/fuels"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Gasolina Comum"))
            .andExpect(jsonPath("$[0].unit").value("LITER"));
    }
}
