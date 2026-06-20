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
class DeleteNozzleUseCaseTest {

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private DeleteNozzleUseCase sut;

    @Test
    void execute_setsDeletedAt() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var station = new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP",
                true, now, now);
        var pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station, now, now);
        var fuel = new Fuel(UUID.randomUUID(), "Gas", FuelUnit.LITERS, true, now);
        var nozzle = new Nozzle(id, 1, pump, fuel, true, now, now);

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(nozzleRepository.save(nozzle)).thenReturn(nozzle);

        sut.execute(id);

        assertThat(nozzle.getDeletedAt()).isNotNull();
        verify(nozzleRepository).save(nozzle);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();

        when(nozzleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
