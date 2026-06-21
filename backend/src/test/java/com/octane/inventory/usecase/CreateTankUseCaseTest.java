package com.octane.inventory.usecase;

import com.octane.inventory.domain.Tank;
import com.octane.inventory.domain.repository.TankRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.StationRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateTankUseCaseTest {

    @Mock
    private TankRepository tankRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private FuelRepository fuelRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CreateTankUseCase sut;

    @Test
    void execute_createsTank() {
        var stationId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A", "SP", "SP",
                true, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(fuelId, "Gasolina", FuelUnit.LITER, true, LocalDateTime.now());
        var request = new CreateTankUseCase.Request(stationId, fuelId, "Tanque 1",
                new BigDecimal("30000"), new BigDecimal("5000"));

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(fuelRepository.findById(fuelId)).thenReturn(Optional.of(fuel));
        when(tankRepository.save(any(Tank.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(request);

        assertThat(result.name()).isEqualTo("Tanque 1");
        assertThat(result.capacity()).isEqualByComparingTo("30000");
        assertThat(result.currentLevel()).isEqualByComparingTo("0");
        verify(tankRepository).save(any(Tank.class));
    }

    @Test
    void execute_throws_whenStationNotFound() {
        var stationId = UUID.randomUUID();
        var request = new CreateTankUseCase.Request(stationId, UUID.randomUUID(), "T1",
                BigDecimal.TEN, BigDecimal.ONE);

        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(tankRepository, never()).save(any());
    }

    @Test
    void execute_throws_whenFuelNotFound() {
        var stationId = UUID.randomUUID();
        var fuelId = UUID.randomUUID();
        var station = new Station(stationId, "P", "00.000.000/0001-00", "A", "C", "SP",
                true, LocalDateTime.now(), LocalDateTime.now());
        var request = new CreateTankUseCase.Request(stationId, fuelId, "T1",
                BigDecimal.TEN, BigDecimal.ONE);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(fuelRepository.findById(fuelId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(tankRepository, never()).save(any());
    }
}
