package com.octane.station.usecase.pump;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.PumpRepository;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeletePumpUseCaseTest {

    @Mock
    private PumpRepository pumpRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DeletePumpUseCase sut;

    @Test
    void execute_setsDeletedAt() {
        var id = UUID.randomUUID();
        var station = new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP",
                true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(id, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());

        when(pumpRepository.findById(id)).thenReturn(Optional.of(pump));
        when(pumpRepository.save(pump)).thenReturn(pump);

        sut.execute(id);

        assertThat(pump.getDeletedAt()).isNotNull();
        verify(pumpRepository).save(pump);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();

        when(pumpRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
