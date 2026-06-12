package com.octane.station.usecase.fuel;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.repository.FuelRepository;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateFuelStatusUseCaseTest {

    @Mock
    private FuelRepository fuelRepository;

    @InjectMocks
    private UpdateFuelStatusUseCase sut;

    @Test
    void execute_deactivatesFuel() {
        var id = UUID.randomUUID();
        var fuel = new Fuel(id, "Diesel S500", FuelUnit.LITER, true, LocalDateTime.now());

        when(fuelRepository.findById(id)).thenReturn(Optional.of(fuel));
        when(fuelRepository.save(any(Fuel.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(id, new UpdateFuelStatusRequest(false));

        assertThat(result.isActive()).isFalse();
        assertThat(result.getName()).isEqualTo("Diesel S500");
    }

    @Test
    void execute_throwsEntityNotFound_whenFuelMissing() {
        var id = UUID.randomUUID();
        when(fuelRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, new UpdateFuelStatusRequest(true)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
