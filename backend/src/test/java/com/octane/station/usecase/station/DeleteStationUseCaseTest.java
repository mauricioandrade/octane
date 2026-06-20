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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteStationUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private DeleteStationUseCase sut;

    @Test
    void execute_setsDeletedAt() {
        var id = UUID.randomUUID();
        var station = new Station(id, "Posto X", "12.345.678/0001-90", "Rua A",
                "SP", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(id)).thenReturn(Optional.of(station));
        when(stationRepository.save(station)).thenReturn(station);

        sut.execute(id);

        assertThat(station.getDeletedAt()).isNotNull();
        verify(stationRepository).save(station);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();

        when(stationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
