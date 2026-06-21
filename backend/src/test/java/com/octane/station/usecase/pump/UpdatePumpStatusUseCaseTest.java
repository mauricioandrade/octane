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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePumpStatusUseCaseTest {

    @Mock
    private PumpRepository pumpRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UpdatePumpStatusUseCase sut;

    @Test
    void execute_setsMaintenanceStatus() {
        var id = UUID.randomUUID();
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(id, 1, PumpStatus.ACTIVE, station, LocalDateTime.now(), LocalDateTime.now());

        when(pumpRepository.findById(id)).thenReturn(Optional.of(pump));
        when(pumpRepository.save(any(Pump.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdatePumpStatusRequest("MAINTENANCE"));

        assertThat(result.getStatus()).isEqualTo(PumpStatus.MAINTENANCE);
    }

    @Test
    void execute_throwsEntityNotFound_whenPumpMissing() {
        var id = UUID.randomUUID();
        when(pumpRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdatePumpStatusRequest("INACTIVE")))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
