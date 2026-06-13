package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
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
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelFuelingUseCaseTest {

    @Mock
    private FuelingRepository fuelingRepository;

    @InjectMocks
    private CancelFuelingUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
        LocalDateTime.now(), LocalDateTime.now());
    private final Fuel fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
        true, LocalDateTime.now());
    private final Nozzle nozzle = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true,
        LocalDateTime.now(), LocalDateTime.now());

    private Fueling buildFueling(Shift shift, FuelingStatus status) {
        return new Fueling(UUID.randomUUID(), shift, nozzle, new BigDecimal("10.000"),
            new BigDecimal("5.00"), new BigDecimal("50.00"), PaymentMethod.PIX,
            status, null, null, null, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void execute_cancelsFueling_whenShiftOpenAndFuelingActive() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
        var fueling = buildFueling(shift, FuelingStatus.ACTIVE);

        when(fuelingRepository.findById(fueling.getId())).thenReturn(Optional.of(fueling));
        when(fuelingRepository.save(any(Fueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(shift.getId(), fueling.getId());

        assertThat(result.getStatus()).isEqualTo(FuelingStatus.CANCELED);
        assertThat(result.getCanceledAt()).isNotNull();
    }

    @Test
    void execute_throwsBusinessException_whenShiftClosed() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.CLOSED,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now());
        var fueling = buildFueling(shift, FuelingStatus.ACTIVE);

        when(fuelingRepository.findById(fueling.getId())).thenReturn(Optional.of(fueling));

        assertThatThrownBy(() -> sut.execute(shift.getId(), fueling.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("aberto");

        verify(fuelingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenAlreadyCanceled() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
        var fueling = buildFueling(shift, FuelingStatus.CANCELED);

        when(fuelingRepository.findById(fueling.getId())).thenReturn(Optional.of(fueling));

        assertThatThrownBy(() -> sut.execute(shift.getId(), fueling.getId()))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("cancelado");
    }

    @Test
    void execute_throwsEntityNotFound_whenFuelingNotInShift() {
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());
        var fueling = buildFueling(shift, FuelingStatus.ACTIVE);

        when(fuelingRepository.findById(fueling.getId())).thenReturn(Optional.of(fueling));

        assertThatThrownBy(() -> sut.execute(UUID.randomUUID(), fueling.getId()))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_throwsEntityNotFound_whenFuelingMissing() {
        var id = UUID.randomUUID();
        when(fuelingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(UUID.randomUUID(), id))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
