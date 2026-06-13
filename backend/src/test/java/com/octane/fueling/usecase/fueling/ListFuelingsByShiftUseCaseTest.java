package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
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
class ListFuelingsByShiftUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private FuelingRepository fuelingRepository;

    @InjectMocks
    private ListFuelingsByShiftUseCase sut;

    private Station makeStation(UUID id) {
        var now = LocalDateTime.now();
        return new Station(id, "Posto A", "00.000.000/0001-00", "Rua X, 1", "Curitiba", "PR", true, now, now);
    }

    private Pump makePump(UUID id, Station station) {
        var now = LocalDateTime.now();
        return new Pump(id, 1, PumpStatus.ACTIVE, station, now, now);
    }

    private Fuel makeFuel(UUID id) {
        return new Fuel(id, "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
    }

    private Nozzle makeNozzle(UUID id, Pump pump) {
        var now = LocalDateTime.now();
        return new Nozzle(id, 1, pump, makeFuel(UUID.randomUUID()), true, now, now);
    }

    private Shift makeShift(UUID id, Station station) {
        var now = LocalDateTime.now();
        return new Shift(id, station, "Funcionario", ShiftStatus.OPEN, now, null, null, now);
    }

    private Fueling makeFueling(UUID id, Shift shift, Nozzle nozzle, BigDecimal liters, BigDecimal unitPrice, BigDecimal totalAmount) {
        var now = LocalDateTime.now();
        return new Fueling(id, shift, nozzle, liters, unitPrice, totalAmount, PaymentMethod.CASH,
                FuelingStatus.ACTIVE, null, null, null, now, now);
    }

    @Test
    void execute_returnsShiftSummaryWithTotals_whenFuelingsExist() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();

        var station = makeStation(stationId);
        var pump = makePump(UUID.randomUUID(), station);
        var nozzle = makeNozzle(UUID.randomUUID(), pump);
        var shift = makeShift(shiftId, station);

        var fueling1 = makeFueling(UUID.randomUUID(), shift, nozzle,
                new BigDecimal("30.000"), new BigDecimal("5.00"), new BigDecimal("150.00"));
        var fueling2 = makeFueling(UUID.randomUUID(), shift, nozzle,
                new BigDecimal("20.000"), new BigDecimal("5.00"), new BigDecimal("100.00"));

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(fuelingRepository.findByShiftId(shiftId)).thenReturn(List.of(fueling1, fueling2));

        var result = sut.execute(shiftId);

        assertThat(result).isNotNull();
        assertThat(result.shiftId()).isEqualTo(shiftId);
        assertThat(result.fuelings()).hasSize(2);
        assertThat(result.totalLiters()).isEqualByComparingTo(new BigDecimal("50.000"));
        assertThat(result.totalAmount()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenShiftNotFound() {
        var shiftId = UUID.randomUUID();

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(shiftId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(shiftId.toString());
    }

    @Test
    void execute_returnsEmptySummaryWithZeroTotals_whenNoFuelings() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();

        var station = makeStation(stationId);
        var shift = makeShift(shiftId, station);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(fuelingRepository.findByShiftId(shiftId)).thenReturn(List.of());

        var result = sut.execute(shiftId);

        assertThat(result.shiftId()).isEqualTo(shiftId);
        assertThat(result.fuelings()).isEmpty();
        assertThat(result.totalLiters()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(result.totalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
