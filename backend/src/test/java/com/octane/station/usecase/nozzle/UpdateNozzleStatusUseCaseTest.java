package com.octane.station.usecase.nozzle;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.NozzleRepository;
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
class UpdateNozzleStatusUseCaseTest {

    @Mock
    private NozzleRepository nozzleRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UpdateNozzleStatusUseCase sut;

    @Test
    void execute_deactivatesNozzle() {
        var id = UUID.randomUUID();
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
            LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
        var nozzle = new Nozzle(id, 1, pump, fuel, true, LocalDateTime.now(), LocalDateTime.now());

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(nozzleRepository.save(any(Nozzle.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdateNozzleStatusRequest(false));

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void execute_throwsEntityNotFound_whenNozzleMissing() {
        var id = UUID.randomUUID();
        when(nozzleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdateNozzleStatusRequest(true)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
