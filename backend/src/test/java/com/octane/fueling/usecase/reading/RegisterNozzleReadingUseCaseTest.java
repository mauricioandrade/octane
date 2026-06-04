package com.octane.fueling.usecase.reading;

import com.octane.fueling.domain.NozzleReading;
import com.octane.fueling.domain.NozzleReadingType;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.NozzleReadingRepository;
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
class RegisterNozzleReadingUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private NozzleReadingRepository nozzleReadingRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private RegisterNozzleReadingUseCase sut;

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

    private NozzleReading makeReading(UUID id, Shift shift, Nozzle nozzle, NozzleReadingType type, BigDecimal totalizer) {
        return new NozzleReading(id, shift, nozzle, type, totalizer, LocalDateTime.now());
    }

    @Test
    void execute_savesAndReturnsReading_whenOpeningHappyPath() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var pump = makePump(UUID.randomUUID(), station);
        var nozzle = makeNozzle(nozzleId, pump);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var request = new RegisterNozzleReadingRequest(nozzleId, "OPENING", new BigDecimal("1000.000"));
        var savedReading = makeReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.OPENING, request.totalizer());

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(nozzleRepository.findById(nozzleId)).thenReturn(Optional.of(nozzle));
        when(nozzleReadingRepository.findByShiftIdAndNozzleIdAndType(shiftId, nozzleId, NozzleReadingType.OPENING))
                .thenReturn(Optional.empty());
        when(nozzleReadingRepository.save(any(NozzleReading.class))).thenReturn(savedReading);

        var result = sut.execute(shiftId, request);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(NozzleReadingType.OPENING);
        assertThat(result.getTotalizer()).isEqualByComparingTo(new BigDecimal("1000.000"));
        verify(nozzleReadingRepository).save(any(NozzleReading.class));
    }

    @Test
    void execute_savesAndReturnsReading_whenClosingHappyPath() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var pump = makePump(UUID.randomUUID(), station);
        var nozzle = makeNozzle(nozzleId, pump);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var openingTotalizer = new BigDecimal("1000.000");
        var closingTotalizer = new BigDecimal("1200.000");
        var request = new RegisterNozzleReadingRequest(nozzleId, "CLOSING", closingTotalizer);
        var openingReading = makeReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.OPENING, openingTotalizer);
        var savedReading = makeReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.CLOSING, closingTotalizer);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(nozzleRepository.findById(nozzleId)).thenReturn(Optional.of(nozzle));
        when(nozzleReadingRepository.findByShiftIdAndNozzleIdAndType(shiftId, nozzleId, NozzleReadingType.CLOSING))
                .thenReturn(Optional.empty());
        when(nozzleReadingRepository.findByShiftIdAndNozzleIdAndType(shiftId, nozzleId, NozzleReadingType.OPENING))
                .thenReturn(Optional.of(openingReading));
        when(nozzleReadingRepository.save(any(NozzleReading.class))).thenReturn(savedReading);

        var result = sut.execute(shiftId, request);

        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(NozzleReadingType.CLOSING);
        verify(nozzleReadingRepository).save(any(NozzleReading.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenShiftNotFound() {
        var shiftId = UUID.randomUUID();
        var request = new RegisterNozzleReadingRequest(UUID.randomUUID(), "OPENING", new BigDecimal("1000.000"));

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(shiftId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(shiftId.toString());

        verify(nozzleReadingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenShiftNotOpen() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var station = makeStation(stationId);
        var shift = makeShift(shiftId, station, ShiftStatus.CLOSED);
        var request = new RegisterNozzleReadingRequest(UUID.randomUUID(), "OPENING", new BigDecimal("1000.000"));

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));

        assertThatThrownBy(() -> sut.execute(shiftId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não está aberto");

        verify(nozzleReadingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenDuplicateReading() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var pump = makePump(UUID.randomUUID(), station);
        var nozzle = makeNozzle(nozzleId, pump);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var request = new RegisterNozzleReadingRequest(nozzleId, "OPENING", new BigDecimal("1000.000"));
        var existingReading = makeReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.OPENING, request.totalizer());

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(nozzleRepository.findById(nozzleId)).thenReturn(Optional.of(nozzle));
        when(nozzleReadingRepository.findByShiftIdAndNozzleIdAndType(shiftId, nozzleId, NozzleReadingType.OPENING))
                .thenReturn(Optional.of(existingReading));

        assertThatThrownBy(() -> sut.execute(shiftId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já registrada");

        verify(nozzleReadingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenClosingTotalizerLessThanOpening() {
        var stationId = UUID.randomUUID();
        var shiftId = UUID.randomUUID();
        var nozzleId = UUID.randomUUID();

        var station = makeStation(stationId);
        var pump = makePump(UUID.randomUUID(), station);
        var nozzle = makeNozzle(nozzleId, pump);
        var shift = makeShift(shiftId, station, ShiftStatus.OPEN);
        var openingTotalizer = new BigDecimal("1000.000");
        var closingTotalizer = new BigDecimal("900.000");
        var request = new RegisterNozzleReadingRequest(nozzleId, "CLOSING", closingTotalizer);
        var openingReading = makeReading(UUID.randomUUID(), shift, nozzle, NozzleReadingType.OPENING, openingTotalizer);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));
        when(nozzleRepository.findById(nozzleId)).thenReturn(Optional.of(nozzle));
        when(nozzleReadingRepository.findByShiftIdAndNozzleIdAndType(shiftId, nozzleId, NozzleReadingType.CLOSING))
                .thenReturn(Optional.empty());
        when(nozzleReadingRepository.findByShiftIdAndNozzleIdAndType(shiftId, nozzleId, NozzleReadingType.OPENING))
                .thenReturn(Optional.of(openingReading));

        assertThatThrownBy(() -> sut.execute(shiftId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fechamento não pode ser menor");

        verify(nozzleReadingRepository, never()).save(any());
    }
}
