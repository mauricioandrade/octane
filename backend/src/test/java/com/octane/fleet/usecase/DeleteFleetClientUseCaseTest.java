package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.usecase.client.DeleteFleetClientUseCase;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteFleetClientUseCaseTest {

    @Mock
    private FleetClientRepository fleetClientRepository;

    @InjectMocks
    private DeleteFleetClientUseCase sut;

    @Test
    void execute_setsDeletedAt() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var station = new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP", true, now, now);
        var client = new FleetClient(id, station, "12.345.678/0001-90", "Empresa X",
                null, BigDecimal.ZERO, true, now);

        when(fleetClientRepository.findById(id)).thenReturn(Optional.of(client));
        when(fleetClientRepository.save(client)).thenReturn(client);

        sut.execute(id);

        assertThat(client.getDeletedAt()).isNotNull();
        verify(fleetClientRepository).save(client);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();

        when(fleetClientRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
