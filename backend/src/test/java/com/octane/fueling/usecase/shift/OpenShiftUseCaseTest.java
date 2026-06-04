package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class OpenShiftUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private OpenShiftUseCase sut;

    private Station makeStation(UUID id) {
        var now = LocalDateTime.now();
        return new Station(id, "Posto A", "00.000.000/0001-00", "Rua X, 1", "Curitiba", "PR", true, now, now);
    }

    private Shift makeShift(UUID id, Station station, ShiftStatus status) {
        var now = LocalDateTime.now();
        return new Shift(id, station, "Funcionario", status, now, null, null, now);
    }

    @Test
    void execute_savesAndReturnsShift_whenStationExistsAndNoOpenShift() {
        var stationId = UUID.randomUUID();
        var station = makeStation(stationId);
        var request = new OpenShiftRequest(stationId, "João", "Turno da manhã");
        var savedShift = makeShift(UUID.randomUUID(), station, ShiftStatus.OPEN);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(shiftRepository.findOpenByStationId(stationId)).thenReturn(Optional.empty());
        when(shiftRepository.save(any(Shift.class))).thenReturn(savedShift);

        var result = sut.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(ShiftStatus.OPEN);
        assertThat(result.getStation().getId()).isEqualTo(stationId);
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenStationNotFound() {
        var stationId = UUID.randomUUID();
        var request = new OpenShiftRequest(stationId, "João", null);

        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(stationId.toString());

        verify(shiftRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenOpenShiftAlreadyExists() {
        var stationId = UUID.randomUUID();
        var station = makeStation(stationId);
        var existingShift = makeShift(UUID.randomUUID(), station, ShiftStatus.OPEN);
        var request = new OpenShiftRequest(stationId, "Maria", null);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(shiftRepository.findOpenByStationId(stationId)).thenReturn(Optional.of(existingShift));

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("turno aberto");

        verify(shiftRepository, never()).save(any());
    }
}
