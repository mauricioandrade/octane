package com.octane.station.usecase.station;

import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListStationsUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private ListStationsUseCase sut;

    @Test
    void execute_returnsAllStations() {
        var stations = List.of(
            new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP",
                true, LocalDateTime.now(), LocalDateTime.now()),
            new Station(UUID.randomUUID(), "Posto Y", "98.765.432/0001-10", "Rua B, 2", "Rio de Janeiro", "RJ",
                true, LocalDateTime.now(), LocalDateTime.now())
        );
        when(stationRepository.findAll()).thenReturn(stations);

        var result = sut.execute();

        assertThat(result).hasSize(2);
        verify(stationRepository).findAll();
    }
}
