package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.FleetDriver;
import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.usecase.driver.DeleteFleetDriverUseCase;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import com.octane.audit.usecase.AuditService;
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
class DeleteFleetDriverUseCaseTest {

    @Mock
    private FleetDriverRepository fleetDriverRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DeleteFleetDriverUseCase sut;

    @Test
    void execute_setsDeletedAt() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var station = new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP", true, now, now);
        var client = new FleetClient(UUID.randomUUID(), station, "00.000.000/0001-00", "E", null, BigDecimal.ZERO, true, now);
        var driver = new FleetDriver(id, client, "João", "123.456.789-00", null, null, true, now);

        when(fleetDriverRepository.findById(id)).thenReturn(Optional.of(driver));
        when(fleetDriverRepository.save(driver)).thenReturn(driver);

        sut.execute(id);

        assertThat(driver.getDeletedAt()).isNotNull();
        verify(fleetDriverRepository).save(driver);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();
        when(fleetDriverRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.execute(id)).isInstanceOf(EntityNotFoundException.class);
    }
}
