package com.octane.commission.handler;

import com.octane.commission.domain.CommissionEntry;
import com.octane.commission.usecase.entry.CalculateCommissionUseCase;
import com.octane.commission.usecase.entry.GetShiftCommissionUseCase;
import com.octane.commission.usecase.entry.ListCommissionEntriesUseCase;
import com.octane.commission.usecase.entry.MarkCommissionPaidUseCase;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.shared.exception.BusinessException;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommissionEntryHandler.class)
class CommissionEntryHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalculateCommissionUseCase calculateCommissionUseCase;

    @MockitoBean
    private ListCommissionEntriesUseCase listCommissionEntriesUseCase;

    @MockitoBean
    private GetShiftCommissionUseCase getShiftCommissionUseCase;

    @MockitoBean
    private MarkCommissionPaidUseCase markCommissionPaidUseCase;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

    private CommissionEntry buildEntry(UUID shiftId) {
        var shift = new Shift(shiftId, station, "João Silva", ShiftStatus.CLOSED,
                LocalDateTime.now().minusHours(8), LocalDateTime.now(), null, LocalDateTime.now());
        return new CommissionEntry(UUID.randomUUID(), shift, "João Silva", station,
                new BigDecimal("1000.00"), new BigDecimal("0.0200"), new BigDecimal("20.00"),
                false, null, LocalDateTime.now());
    }

    @Test
    void postCalculate_returns200_withCommission() throws Exception {
        var shiftId = UUID.randomUUID();
        var entry = buildEntry(shiftId);

        when(calculateCommissionUseCase.execute(shiftId)).thenReturn(Optional.of(entry));

        mockMvc.perform(post("/api/commission/calculate/" + shiftId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commission").value(20.00))
                .andExpect(jsonPath("$.employeeName").value("João Silva"));
    }

    @Test
    void postCalculate_returns204_whenNoRule() throws Exception {
        var shiftId = UUID.randomUUID();

        when(calculateCommissionUseCase.execute(shiftId)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/commission/calculate/" + shiftId))
                .andExpect(status().isNoContent());
    }

    @Test
    void postPay_returns422_whenAlreadyPaid() throws Exception {
        var entryId = UUID.randomUUID();

        when(markCommissionPaidUseCase.execute(entryId))
                .thenThrow(new BusinessException("Comissão já marcada como paga"));

        mockMvc.perform(post("/api/commission/entries/" + entryId + "/pay"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Comissão já marcada como paga"));
    }
}
