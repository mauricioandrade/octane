package com.octane.station.usecase.station;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
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
class UpdateStationUseCaseTest {

    @Mock
    private StationRepository stationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UpdateStationUseCase sut;

    private Station buildStation(UUID id, String cnpj) {
        return new Station(id, "Posto X", cnpj, "Rua A, 1", "São Paulo", "SP",
            true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void execute_updatesAndReturnsStation_whenFound() {
        var id = UUID.randomUUID();
        var existing = buildStation(id, "12.345.678/0001-90");
        var request = new UpdateStationRequest("Posto Novo", "12.345.678/0001-90", "Rua B, 2", "Campinas", "SP");

        when(stationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(stationRepository.findByCnpj("12.345.678/0001-90")).thenReturn(Optional.of(existing));
        when(stationRepository.save(any(Station.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, request);

        assertThat(result.getId()).isEqualTo(id);
        assertThat(result.getName()).isEqualTo("Posto Novo");
        assertThat(result.getCity()).isEqualTo("Campinas");
        assertThat(result.isActive()).isTrue();
        verify(stationRepository).save(any(Station.class));
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var id = UUID.randomUUID();
        when(stationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id,
            new UpdateStationRequest("Posto", "12.345.678/0001-90", "Rua", "SP", "SP")))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_throwsBusinessException_whenCnpjBelongsToAnotherStation() {
        var id = UUID.randomUUID();
        var existing = buildStation(id, "12.345.678/0001-90");
        var other = buildStation(UUID.randomUUID(), "99.999.999/0001-99");

        when(stationRepository.findById(id)).thenReturn(Optional.of(existing));
        when(stationRepository.findByCnpj("99.999.999/0001-99")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> sut.execute(id,
            new UpdateStationRequest("Posto", "99.999.999/0001-99", "Rua", "SP", "SP")))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("CNPJ");

        verify(stationRepository, never()).save(any());
    }
}
