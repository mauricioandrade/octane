package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftReconciliation;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftReconciliationRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetShiftReconciliationUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private ShiftReconciliationRepository shiftReconciliationRepository;

    @InjectMocks
    private GetShiftReconciliationUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
        LocalDateTime.now(), LocalDateTime.now());
    private final Fuel fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
        true, LocalDateTime.now());
    private final Nozzle nozzle = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true,
        LocalDateTime.now(), LocalDateTime.now());

    @Test
    void execute_returnsLinesAndTotals_whenShiftClosed() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.CLOSED,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now());
        var line = new ShiftReconciliation(UUID.randomUUID(), shift, nozzle,
            new BigDecimal("1000.000"), new BigDecimal("1100.000"),
            new BigDecimal("100.000"), new BigDecimal("95.500"), new BigDecimal("4.500"),
            LocalDateTime.now());

        when(shiftRepository.findById(shift.getId())).thenReturn(Optional.of(shift));
        when(shiftReconciliationRepository.findByShiftId(shift.getId())).thenReturn(List.of(line));

        var result = sut.execute(shift.getId());

        assertThat(result.lines()).hasSize(1);
        assertThat(result.lines().get(0).nozzleNumber()).isEqualTo(1);
        assertThat(result.lines().get(0).fuelName()).isEqualTo("Gasolina Comum");
        assertThat(result.totalMeasuredLiters()).isEqualByComparingTo("100.000");
        assertThat(result.totalFueledLiters()).isEqualByComparingTo("95.500");
        assertThat(result.totalDivergenceLiters()).isEqualByComparingTo("4.500");
    }

    @Test
    void execute_throwsBusinessException_whenShiftStillOpen() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
        when(shiftRepository.findById(shift.getId())).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> sut.execute(shift.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("fechado");
    }

    @Test
    void execute_throwsEntityNotFound_whenShiftMissing() {
        var id = UUID.randomUUID();
        when(shiftRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
