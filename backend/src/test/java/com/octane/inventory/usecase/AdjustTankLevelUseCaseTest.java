package com.octane.inventory.usecase;

import com.octane.inventory.domain.Tank;
import com.octane.inventory.domain.TankMovement;
import com.octane.inventory.domain.repository.TankRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdjustTankLevelUseCaseTest {

    @Mock
    private TankRepository tankRepository;

    @InjectMocks
    private AdjustTankLevelUseCase sut;

    private Tank createTank(BigDecimal currentLevel) {
        var tank = new Tank();
        tank.setStation(new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP",
                true, LocalDateTime.now(), LocalDateTime.now()));
        tank.setFuel(new Fuel(UUID.randomUUID(), "Gasolina", FuelUnit.LITER, true, LocalDateTime.now()));
        tank.setName("Tanque 1");
        tank.setCapacity(new BigDecimal("30000"));
        tank.setCurrentLevel(currentLevel);
        tank.setMinimumLevel(new BigDecimal("5000"));
        tank.setActive(true);
        tank.setCreatedAt(LocalDateTime.now());
        return tank;
    }

    @Test
    void execute_adjustsLevel_upward() {
        var tankId = UUID.randomUUID();
        var tank = createTank(new BigDecimal("10000"));
        var request = new AdjustTankLevelUseCase.Request(new BigDecimal("12000"), "Aferição");

        when(tankRepository.findById(tankId)).thenReturn(Optional.of(tank));
        when(tankRepository.save(any(Tank.class))).thenReturn(tank);
        when(tankRepository.saveMovement(any(TankMovement.class))).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(tankId, request);

        assertThat(tank.getCurrentLevel()).isEqualByComparingTo("12000");
        verify(tankRepository).save(tank);
        verify(tankRepository).saveMovement(any(TankMovement.class));
    }

    @Test
    void execute_adjustsLevel_downward() {
        var tankId = UUID.randomUUID();
        var tank = createTank(new BigDecimal("10000"));
        var request = new AdjustTankLevelUseCase.Request(new BigDecimal("8000"), "Perda detectada");

        when(tankRepository.findById(tankId)).thenReturn(Optional.of(tank));
        when(tankRepository.save(any(Tank.class))).thenReturn(tank);
        when(tankRepository.saveMovement(any(TankMovement.class))).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(tankId, request);

        assertThat(tank.getCurrentLevel()).isEqualByComparingTo("8000");
    }

    @Test
    void execute_throws_whenTankNotFound() {
        var tankId = UUID.randomUUID();
        var request = new AdjustTankLevelUseCase.Request(BigDecimal.TEN, null);

        when(tankRepository.findById(tankId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(tankId, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
