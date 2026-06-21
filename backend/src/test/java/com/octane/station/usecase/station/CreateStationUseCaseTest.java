package com.octane.station.usecase.station;

import com.octane.shared.exception.BusinessException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import com.octane.audit.usecase.AuditService;
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
class CreateStationUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CreateStationUseCase sut;

    @Test
    void execute_savesAndReturnsStation_whenCnpjIsNew() {
        var request = new CreateStationRequest("Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP");
        var saved = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.empty());
        when(stationRepository.save(any(Station.class))).thenReturn(saved);

        var result = sut.execute(request);

        assertThat(result.getId()).isEqualTo(saved.getId());
        assertThat(result.getCnpj()).isEqualTo("12.345.678/0001-90");
        verify(stationRepository).save(any(Station.class));
    }

    @Test
    void execute_throwsBusinessException_whenCnpjAlreadyExists() {
        var request = new CreateStationRequest("Posto X", "12.345.678/0001-90", "Rua A, 1", "São Paulo", "SP");
        var existing = new Station(UUID.randomUUID(), "Posto Y", "12.345.678/0001-90", "Rua B, 2",
            "Rio de Janeiro", "RJ", true, LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> sut.execute(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CNPJ");

        verify(stationRepository, never()).save(any());
    }
}
