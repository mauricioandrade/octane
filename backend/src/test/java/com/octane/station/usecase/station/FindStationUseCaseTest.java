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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindStationUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private FindStationUseCase sut;

    @Test
    void execute_returnsStation_whenFound() {
        var id = UUID.randomUUID();
        var station = new Station(id, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(id)).thenReturn(Optional.of(station));

        var result = sut.execute(id);

        assertThat(result.getId()).isEqualTo(id);
    }

    @Test
    void execute_throwsEntityNotFoundException_whenNotFound() {
        var id = UUID.randomUUID();
        when(stationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining(id.toString());
    }
}
