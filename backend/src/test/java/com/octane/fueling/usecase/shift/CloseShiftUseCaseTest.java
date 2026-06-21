package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.NozzleReading;
import com.octane.fueling.domain.NozzleReadingType;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftReconciliation;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.NozzleReadingRepository;
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
import com.octane.station.domain.repository.NozzleRepository;
import com.octane.station.domain.repository.PumpRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import com.octane.audit.usecase.AuditService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseShiftUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private NozzleReadingRepository nozzleReadingRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @Mock
    private PumpRepository pumpRepository;

    @Mock
    private FuelingRepository fuelingRepository;

    @Mock
    private ShiftReconciliationRepository shiftReconciliationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CloseShiftUseCase sut;

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

    private Nozzle makeNozzle(UUID id, Pump pump, boolean active) {
        var now = LocalDateTime.now();
        return new Nozzle(id, 1, pump, makeFuel(UUID.randomUUID()), active, now, now);
    }

    private Shift makeShift(UUID id, Station station, ShiftStatus status) {
        var now = LocalDateTime.now();
        return new Shift(id, station, "Funcionario", status, now, null, null, now);
    }

    private NozzleReading makeClosingReading(Shift shift, Nozzle nozzle) {
        return new NozzleReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.CLOSING,
                BigDecimal.valueOf(1000.0), LocalDateTime.now());
    }

    @Test
    void execute_closesShiftAndReturns_whenAllConditionsMet() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var pumpId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var pump = makePump(pumpId, station);
        var nozzle = makeNozzle(nozzleId, pump, true);
        var closingReading = makeClosingReading(shift, nozzle);
        var closedShift = makeShift(shiftId, station, ShiftStatus.CLOSED);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(pumpRepository.findByStationId(stationId)).thenReturn(List.of(pump));
        when(nozzleRepository.findByPumpId(pumpId)).thenReturn(List.of(nozzle));
        when(nozzleReadingRepository.findByShiftId(shiftId)).thenReturn(List.of(closingReading));
        when(shiftRepository.save(any(Shift.class))).thenReturn(closedShift);

        var result = sut.execute(shiftId);

        assertThat(result.getStatus()).isEqualTo(ShiftStatus.CLOSED);
        verify(shiftRepository).save(any(Shift.class));
    }

    @SuppressWarnings("unchecked")
    @Test
    void execute_persistsReconciliationPerNozzle_onClose() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var pumpId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var pump = makePump(pumpId, station);
        var nozzle = makeNozzle(nozzleId, pump, true);

        var opening = new NozzleReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.OPENING,
                new BigDecimal("1000.000"), LocalDateTime.now());
        var closing = new NozzleReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.CLOSING,
                new BigDecimal("1100.000"), LocalDateTime.now());

        var now = LocalDateTime.now();
        var activeFueling = new Fueling(UUID.randomUUID(), shift, nozzle, new BigDecimal("95.500"),
                new BigDecimal("5.00"), new BigDecimal("477.50"), PaymentMethod.PIX,
                FuelingStatus.ACTIVE, null, null, null, now, now);
        var canceledFueling = new Fueling(UUID.randomUUID(), shift, nozzle, new BigDecimal("10.000"),
                new BigDecimal("5.00"), new BigDecimal("50.00"), PaymentMethod.PIX,
                FuelingStatus.CANCELED, now, null, null, now, now);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(pumpRepository.findByStationId(stationId)).thenReturn(List.of(pump));
        when(nozzleRepository.findByPumpId(pumpId)).thenReturn(List.of(nozzle));
        when(nozzleReadingRepository.findByShiftId(shiftId)).thenReturn(List.of(opening, closing));
        when(fuelingRepository.findByShiftId(shiftId)).thenReturn(List.of(activeFueling, canceledFueling));
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(shiftId);

        ArgumentCaptor<List<ShiftReconciliation>> captor = ArgumentCaptor.forClass(List.class);
        verify(shiftReconciliationRepository).saveAll(captor.capture());
        var lines = captor.getValue();
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getNozzle().getId()).isEqualTo(nozzleId);
        assertThat(lines.get(0).getOpeningTotalizer()).isEqualByComparingTo("1000.000");
        assertThat(lines.get(0).getClosingTotalizer()).isEqualByComparingTo("1100.000");
        assertThat(lines.get(0).getMeasuredLiters()).isEqualByComparingTo("100.000");
        assertThat(lines.get(0).getFueledLiters()).isEqualByComparingTo("95.500");
        assertThat(lines.get(0).getDivergenceLiters()).isEqualByComparingTo("4.500");
    }

    @SuppressWarnings("unchecked")
    @Test
    void execute_usesZeroAsOpeningTotalizer_whenNozzleHasNoOpeningReading() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var pumpId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var pump = makePump(pumpId, station);
        var nozzle = makeNozzle(nozzleId, pump, true);
        var closing = makeClosingReading(shift, nozzle); // totalizer = 1000.0

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(pumpRepository.findByStationId(stationId)).thenReturn(List.of(pump));
        when(nozzleRepository.findByPumpId(pumpId)).thenReturn(List.of(nozzle));
        when(nozzleReadingRepository.findByShiftId(shiftId)).thenReturn(List.of(closing));
        when(fuelingRepository.findByShiftId(shiftId)).thenReturn(List.of());
        when(shiftRepository.save(any(Shift.class))).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(shiftId);

        ArgumentCaptor<List<ShiftReconciliation>> captor = ArgumentCaptor.forClass(List.class);
        verify(shiftReconciliationRepository).saveAll(captor.capture());
        var lines = captor.getValue();
        assertThat(lines).hasSize(1);
        assertThat(lines.get(0).getOpeningTotalizer()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(lines.get(0).getClosingTotalizer()).isEqualByComparingTo("1000.0");
        assertThat(lines.get(0).getMeasuredLiters()).isEqualByComparingTo("1000.0");
        assertThat(lines.get(0).getDivergenceLiters()).isEqualByComparingTo("1000.0");
    }

    @Test
    void execute_throwsEntityNotFoundException_whenShiftNotFound() {
        var shiftId = UUID.randomUUID();
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(shiftId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(shiftId.toString());

        verify(shiftRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenShiftAlreadyClosed() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var station = makeStation(stationId);
        var shift = makeShift(shiftId, station, ShiftStatus.CLOSED);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> sut.execute(shiftId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fechado");

        verify(shiftRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenMissingClosingReadings() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var pumpId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var pump = makePump(pumpId, station);
        var nozzle = makeNozzle(nozzleId, pump, true);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(pumpRepository.findByStationId(stationId)).thenReturn(List.of(pump));
        when(nozzleRepository.findByPumpId(pumpId)).thenReturn(List.of(nozzle));
        when(nozzleReadingRepository.findByShiftId(shiftId)).thenReturn(List.of());

        assertThatThrownBy(() -> sut.execute(shiftId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("leituras de fechamento");

        verify(shiftRepository, never()).save(any());
    }
}
