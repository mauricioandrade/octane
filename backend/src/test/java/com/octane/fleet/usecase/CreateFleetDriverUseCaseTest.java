package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.FleetDriver;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.usecase.driver.CreateFleetDriverRequest;
import com.octane.fleet.usecase.driver.CreateFleetDriverUseCase;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
class CreateFleetDriverUseCaseTest {

    @Mock
    private FleetDriverRepository fleetDriverRepository;

    @Mock
    private FleetClientRepository fleetClientRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateFleetDriverUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

    private final FleetClient client = new FleetClient(UUID.randomUUID(), station, "12.345.678/0001-90",
            "Empresa LTDA", null, null, true, LocalDateTime.now());

    @Test
    void execute_throwsBusinessException_whenNeitherPinNorRfid() {
        var request = new CreateFleetDriverRequest(client.getId(), "João Silva",
                "123.456.789-09", null, null);

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("PIN ou RFID");

        verify(fleetDriverRepository, never()).save(any());
    }

    @Test
    void execute_hashesPin_whenPinProvided() {
        var request = new CreateFleetDriverRequest(client.getId(), "João Silva",
                "123.456.789-09", "123456", null);

        when(fleetClientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(fleetDriverRepository.findByCpfAndClientId(request.cpf(), client.getId()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("123456")).thenReturn("hashed-pin");
        when(fleetDriverRepository.save(any(FleetDriver.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(request);

        assertThat(result.hasPIN()).isTrue();
        assertThat(result.hasRFID()).isFalse();
        verify(passwordEncoder).encode("123456");
    }

    @Test
    void execute_throwsBusinessException_whenCpfAlreadyExistsForClient() {
        var request = new CreateFleetDriverRequest(client.getId(), "João Silva",
                "123.456.789-09", "123456", null);
        var existing = new FleetDriver(UUID.randomUUID(), client, "João", "123.456.789-09",
                "hash", null, true, LocalDateTime.now());

        when(fleetClientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(fleetDriverRepository.findByCpfAndClientId(request.cpf(), client.getId()))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");

        verify(fleetDriverRepository, never()).save(any());
    }

    @Test
    void execute_throwsEntityNotFoundException_whenClientNotFound() {
        var clientId = UUID.randomUUID();
        var request = new CreateFleetDriverRequest(clientId, "João Silva",
                "123.456.789-09", "123456", null);

        when(fleetClientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_createsDriver_withRfidOnly() {
        var request = new CreateFleetDriverRequest(client.getId(), "Maria Santos",
                "987.654.321-00", null, "RFID-TAG-001");

        when(fleetClientRepository.findById(client.getId())).thenReturn(Optional.of(client));
        when(fleetDriverRepository.findByCpfAndClientId(request.cpf(), client.getId()))
                .thenReturn(Optional.empty());
        when(fleetDriverRepository.save(any(FleetDriver.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(request);

        assertThat(result.hasPIN()).isFalse();
        assertThat(result.hasRFID()).isTrue();
        verify(passwordEncoder, never()).encode(any());
    }
}
