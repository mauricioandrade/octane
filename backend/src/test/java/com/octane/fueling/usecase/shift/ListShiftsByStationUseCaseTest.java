package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.shared.pagination.PageResponse;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListShiftsByStationUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private ListShiftsByStationUseCase sut;

    @Test
    void execute_returnsPagedShifts_parsingStatus() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var shift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.OPEN,
            LocalDateTime.now(), null, null, LocalDateTime.now());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(shiftRepository.findByStationId(stationId, ShiftStatus.OPEN, null, null, 0, 20))
            .thenReturn(PageResponse.of(List.of(shift), 0, 20, 1));

        var result = sut.execute(stationId, "OPEN", null, null, 0, 20);

        assertThat(result.content()).hasSize(1);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    void execute_passesNullStatus_whenNotGiven() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(shiftRepository.findByStationId(stationId, null, null, null, 1, 10))
            .thenReturn(PageResponse.of(List.of(), 1, 10, 0));

        var result = sut.execute(stationId, null, null, null, 1, 10);

        assertThat(result.content()).isEmpty();
        assertThat(result.page()).isEqualTo(1);
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId, null, null, null, 0, 20))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
