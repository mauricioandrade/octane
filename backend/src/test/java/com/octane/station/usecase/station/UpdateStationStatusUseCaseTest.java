package com.octane.station.usecase.station;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateStationStatusUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private UpdateStationStatusUseCase sut;

    @Test
    void execute_deactivatesStation() {
        var id = UUID.randomUUID();
        var existing = new Station(id, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdateStationStatusRequest(false));

        assertThat(result.isActive()).isFalse();
        assertThat(result.getName()).isEqualTo("Posto X");
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var id = UUID.randomUUID();
        when(stationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdateStationStatusRequest(true)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
