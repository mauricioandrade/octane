package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.usecase.client.CreateFleetClientRequest;
import com.octane.fleet.usecase.client.CreateFleetClientUseCase;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class CreateFleetClientUseCaseTest {

    @Mock
    private FleetClientRepository fleetClientRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private CreateFleetClientUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

    @Test
    void execute_createsClient_withValidRequest() {
        var request = new CreateFleetClientRequest(station.getId(), "12.345.678/0001-90",
                "Empresa LTDA", "Fantasia", new BigDecimal("5000.00"));

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fleetClientRepository.findByCnpjAndStationId(request.cnpj(), station.getId()))
                .thenReturn(Optional.empty());
        when(fleetClientRepository.save(any(FleetClient.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(request);

        assertThat(result.cnpj()).isEqualTo("12.345.678/0001-90");
        assertThat(result.companyName()).isEqualTo("Empresa LTDA");
        assertThat(result.active()).isTrue();
        verify(fleetClientRepository).save(any(FleetClient.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenStationNotFound() {
        var stationId = UUID.randomUUID();
        var request = new CreateFleetClientRequest(stationId, "12.345.678/0001-90",
                "Empresa LTDA", null, null);

        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(fleetClientRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenCnpjAlreadyExists() {
        var request = new CreateFleetClientRequest(station.getId(), "12.345.678/0001-90",
                "Empresa LTDA", null, null);
        var existing = new FleetClient(UUID.randomUUID(), station, "12.345.678/0001-90",
                "Outra Empresa", null, null, true, LocalDateTime.now());

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fleetClientRepository.findByCnpjAndStationId(request.cnpj(), station.getId()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CNPJ");

        verify(fleetClientRepository, never()).save(any());
    }
}
