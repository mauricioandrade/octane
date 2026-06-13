package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.NozzleRepository;
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
class RegisterFuelingUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private FuelingRepository fuelingRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private RegisterFuelingUseCase sut;

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

    private Shift makeShift(UUID id, Station station, ShiftStatus status) {
        var now = LocalDateTime.now();
        return new Shift(id, station, "Funcionario", status, now, null, null, now);
    }

    private Fueling makeFueling(UUID id, Shift shift, Nozzle nozzle) {
        var now = LocalDateTime.now();
        return new Fueling(id, shift, nozzle,
                new BigDecimal("50.000"), new BigDecimal("5.8900"), new BigDecimal("294.50"),
                PaymentMethod.PIX, com.octane.fueling.domain.FuelingStatus.ACTIVE, null,
                "ABC-1234", null, now, now);
    }

    @Test
    void execute_savesAndReturnsFueling_whenHappyPath() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var pump = makePump(UUID.randomUUID(), station);
        var nozzle = makeNozzle(nozzleId, pump);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        // 50 liters * 5.89 = 294.50
        var request = new RegisterFuelingRequest(
                nozzleId,
                new BigDecimal("50.000"),
                new BigDecimal("5.8900"),
                new BigDecimal("294.50"),
                "PIX", "ABC-1234", null
        );
        var savedFueling = makeFueling(UUID.randomUUID(), shift, nozzle);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(nozzleRepository.findById(nozzleId)).thenReturn(Optional.of(nozzle));
        when(fuelingRepository.save(any(Fueling.class))).thenReturn(savedFueling);

        var result = sut.execute(shiftId, request);

        assertThat(result).isNotNull();
        assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.PIX);
        verify(fuelingRepository).save(any(Fueling.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenShiftNotFound() {
        var shiftId = UUID.randomUUID();
        var request = new RegisterFuelingRequest(
                UUID.randomUUID(), new BigDecimal("10.000"), new BigDecimal("5.00"),
                new BigDecimal("50.00"), "CASH", null, null
        );

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(shiftId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(shiftId.toString());

        verify(fuelingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenShiftNotOpen() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var station = makeStation(stationId);
        var shift = makeShift(shiftId, station, ShiftStatus.CLOSED);
        var request = new RegisterFuelingRequest(
                UUID.randomUUID(), new BigDecimal("10.000"), new BigDecimal("5.00"),
                new BigDecimal("50.00"), "CASH", null, null
        );

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> sut.execute(shiftId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está aberto");

        verify(fuelingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenNozzleNotInStation() {
        var stationId = UUID.randomUUID();
        var otherStationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var otherStation = makeStation(otherStationId);
        var pump = makePump(UUID.randomUUID(), otherStation);
        var nozzle = makeNozzle(nozzleId, pump);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var request = new RegisterFuelingRequest(
                nozzleId, new BigDecimal("10.000"), new BigDecimal("5.00"),
                new BigDecimal("50.00"), "CASH", null, null
        );

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(nozzleRepository.findById(nozzleId)).thenReturn(Optional.of(nozzle));

        assertThatThrownBy(() -> sut.execute(shiftId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Bico não pertence ao posto");

        verify(fuelingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenTotalAmountMismatch() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var pump = makePump(UUID.randomUUID(), station);
        var nozzle = makeNozzle(nozzleId, pump);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        // 10 liters * 5.00 = 50.00, but we pass 99.99
        var request = new RegisterFuelingRequest(
                nozzleId, new BigDecimal("10.000"), new BigDecimal("5.00"),
                new BigDecimal("99.99"), "CASH", null, null
        );

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(nozzleRepository.findById(nozzleId)).thenReturn(Optional.of(nozzle));

        assertThatThrownBy(() -> sut.execute(shiftId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Valor total não confere");

        verify(fuelingRepository, never()).save(any());
    }
}
