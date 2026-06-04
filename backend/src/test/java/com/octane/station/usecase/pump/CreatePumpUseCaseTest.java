package com.octane.station.usecase.pump;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.PumpRepository;
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
class CreatePumpUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private PumpRepository pumpRepository;

    @InjectMocks
    private CreatePumpUseCase sut;

    @Test
    void execute_savesAndReturnsPump_whenNumberIsAvailable() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var request = new CreatePumpRequest(1);
        var saved = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
            LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pumpRepository.existsByStationIdAndNumber(stationId, 1)).thenReturn(false);
        when(pumpRepository.save(any(Pump.class))).thenReturn(saved);

        var result = sut.execute(stationId, request);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getStatus()).isEqualTo(PumpStatus.ACTIVE);
        verify(pumpRepository).save(any(Pump.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenStationNotFound() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId, new CreatePumpRequest(1)))
            .isInstanceOf(EntityNotFoundException.class);

        verify(pumpRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenNumberAlreadyUsed() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(pumpRepository.existsByStationIdAndNumber(stationId, 1)).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(stationId, new CreatePumpRequest(1)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("1");

        verify(pumpRepository, never()).save(any());
    }
}
