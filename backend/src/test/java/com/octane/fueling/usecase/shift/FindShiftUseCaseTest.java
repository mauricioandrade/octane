package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindShiftUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private FindShiftUseCase sut;

    private Station makeStation(UUID id) {
        var now = LocalDateTime.now();
        return new Station(id, "Posto A", "00.000.000/0001-00", "Rua X, 1", "Curitiba", "PR", true, now, now);
    }

    private Shift makeShift(UUID id, Station station) {
        var now = LocalDateTime.now();
        return new Shift(id, station, "Funcionario", ShiftStatus.OPEN, now, null, null, now);
    }

    @Test
    void execute_returnsShift_whenFound() {
        var shiftId = UUID.randomUUID();
        var station = makeStation(UUID.randomUUID());
        var shift = makeShift(shiftId, station);

        when(shiftRepository.findById(shiftId)).thenReturn(Optional.of(shift));

        var result = sut.execute(shiftId);

        assertThat(result.getId()).isEqualTo(shiftId);
        assertThat(result.getStatus()).isEqualTo(ShiftStatus.OPEN);
    }

    @Test
    void execute_throwsEntityNotFoundException_whenNotFound() {
        var shiftId = UUID.randomUUID();
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(shiftId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining(shiftId.toString());
    }
}
