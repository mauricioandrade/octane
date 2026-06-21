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
class UpdateNozzleUseCaseTest {

    @Mock
    private NozzleRepository nozzleRepository;

    @Mock
    private FuelRepository fuelRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private UpdateNozzleUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
        LocalDateTime.now(), LocalDateTime.now());
    private final Fuel gasolina = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
        true, LocalDateTime.now());
    private final Fuel etanol = new Fuel(UUID.randomUUID(), "Etanol", FuelUnit.LITER,
        true, LocalDateTime.now());

    @Test
    void execute_updatesNumberAndFuel() {
        var id = UUID.randomUUID();
        var nozzle = new Nozzle(id, 1, pump, gasolina, true, LocalDateTime.now(), LocalDateTime.now());

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(fuelRepository.findById(etanol.getId())).thenReturn(Optional.of(etanol));
        when(nozzleRepository.existsByPumpIdAndNumber(pump.getId(), 2)).thenReturn(false);
        when(nozzleRepository.save(any(Nozzle.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdateNozzleRequest(2, etanol.getId()));

        assertThat(result.getNumber()).isEqualTo(2);
        assertThat(result.getFuel().getId()).isEqualTo(etanol.getId());
    }

    @Test
    void execute_throwsBusinessException_whenNumberDuplicatedOnPump() {
        var id = UUID.randomUUID();
        var nozzle = new Nozzle(id, 1, pump, gasolina, true, LocalDateTime.now(), LocalDateTime.now());

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(fuelRepository.findById(gasolina.getId())).thenReturn(Optional.of(gasolina));
        when(nozzleRepository.existsByPumpIdAndNumber(pump.getId(), 2)).thenReturn(true);

        assertThatThrownBy(() -> sut.execute(id, new UpdateNozzleRequest(2, gasolina.getId())))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Bico");

        verify(nozzleRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenFuelInactive() {
        var id = UUID.randomUUID();
        var nozzle = new Nozzle(id, 1, pump, gasolina, true, LocalDateTime.now(), LocalDateTime.now());
        var inactiveFuel = new Fuel(UUID.randomUUID(), "Diesel S500", FuelUnit.LITER, false, LocalDateTime.now());

        when(nozzleRepository.findById(id)).thenReturn(Optional.of(nozzle));
        when(fuelRepository.findById(inactiveFuel.getId())).thenReturn(Optional.of(inactiveFuel));

        assertThatThrownBy(() -> sut.execute(id, new UpdateNozzleRequest(1, inactiveFuel.getId())))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativo");
    }

    @Test
    void execute_throwsEntityNotFound_whenNozzleMissing() {
        var id = UUID.randomUUID();
        when(nozzleRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdateNozzleRequest(1, UUID.randomUUID())))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
