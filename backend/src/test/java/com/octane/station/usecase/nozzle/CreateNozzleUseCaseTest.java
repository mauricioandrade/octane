package com.octane.station.usecase.nozzle;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.NozzleRepository;
import com.octane.station.domain.repository.PumpRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateNozzleUseCaseTest {

    @Mock
    private PumpRepository pumpRepository;

    @Mock
    private FuelRepository fuelRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @InjectMocks
    private CreateNozzleUseCase sut;

    private Station buildStation() {
        return new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "SP", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    }

    private Pump buildPump(UUID pumpId) {
        return new Pump(pumpId, 1, PumpStatus.ACTIVE, buildStation(),
            LocalDateTime.now(), LocalDateTime.now());
    }

    private Fuel buildFuel(UUID fuelId) {
        return new Fuel(fuelId, "Gasolina Comum", FuelUnit.LITER, true, LocalDateTime.now());
    }

    @Test
    void execute_savesNozzle_whenInputsAreValid() {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        var request = new CreateNozzleRequest(1, fuelId);
        var pump = buildPump(pumpId);
        var fuel = buildFuel(fuelId);
        var saved = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true, LocalDateTime.now(), LocalDateTime.now());

        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(pump));
        when(fuelRepository.findById(fuelId)).thenReturn(Optional.of(fuel));
        when(nozzleRepository.existsByPumpIdAndNumber(pumpId, 1)).thenReturn(false);
        when(nozzleRepository.save(any(Nozzle.class))).thenReturn(saved);

        var result = sut.execute(pumpId, request);

        assertThat(result.getNumber()).isEqualTo(1);
        assertThat(result.getFuel().getId()).isEqualTo(fuelId);
        verify(nozzleRepository).save(any(Nozzle.class));
    }

    @Test
    void execute_throwsEntityNotFoundException_whenPumpNotFound() {
        var pumpId = UUID.randomUUID();
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(pumpId, new CreateNozzleRequest(1, UUID.randomUUID())))
            .isInstanceOf(EntityNotFoundException.class);

        verify(nozzleRepository, never()).save(any());
    }

    @Test
    void execute_throwsEntityNotFoundException_whenFuelNotFound() {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(buildPump(pumpId)));
        when(fuelRepository.findById(fuelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(pumpId, new CreateNozzleRequest(1, fuelId)))
            .isInstanceOf(EntityNotFoundException.class);

        verify(nozzleRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenNozzleNumberAlreadyUsed() {
        var pumpId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        when(pumpRepository.findById(pumpId)).thenReturn(Optional.of(buildPump(pumpId)));
        when(fuelRepository.findById(fuelId)).thenReturn(Optional.of(buildFuel(fuelId)));
        when(nozzleRepository.existsByPumpIdAndNumber(pumpId, 1)).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(pumpId, new CreateNozzleRequest(1, fuelId)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("1");

        verify(nozzleRepository, never()).save(any());
    }
}
