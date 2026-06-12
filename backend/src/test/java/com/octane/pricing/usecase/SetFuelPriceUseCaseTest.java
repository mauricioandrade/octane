package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.StationRepository;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SetFuelPriceUseCaseTest {

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private FuelRepository fuelRepository;

    @InjectMocks
    private SetFuelPriceUseCase sut;

    private Station buildStation(boolean active) {
        return new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", active, LocalDateTime.now(), LocalDateTime.now());
    }

    private Fuel buildFuel(boolean active) {
        return new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER, active, LocalDateTime.now());
    }

    @Test
    void execute_savesPrice_whenStationAndFuelActive() {
        var station = buildStation(true);
        var fuel = buildFuel(true);

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelRepository.findById(fuel.getId())).thenReturn(Optional.of(fuel));
        when(fuelPriceRepository.save(any(FuelPrice.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(station.getId(), new SetFuelPriceRequest(fuel.getId(), new BigDecimal("5.8990")));

        assertThat(result.getPrice()).isEqualByComparingTo("5.8990");
        assertThat(result.getStation().getId()).isEqualTo(station.getId());
        assertThat(result.getFuel().getId()).isEqualTo(fuel.getId());
        assertThat(result.getEffectiveFrom()).isNotNull();
    }

    @Test
    void execute_throwsBusinessException_whenPriceNotPositive() {
        var station = buildStation(true);
        var fuel = buildFuel(true);
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelRepository.findById(fuel.getId())).thenReturn(Optional.of(fuel));

        assertThatThrownBy(() -> sut.execute(station.getId(),
            new SetFuelPriceRequest(fuel.getId(), BigDecimal.ZERO)))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Preço");

        verify(fuelPriceRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenStationInactive() {
        var station = buildStation(false);
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));

        assertThatThrownBy(() -> sut.execute(station.getId(),
            new SetFuelPriceRequest(UUID.randomUUID(), new BigDecimal("5.89"))))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativo");
    }

    @Test
    void execute_throwsBusinessException_whenFuelInactive() {
        var station = buildStation(true);
        var fuel = buildFuel(false);
        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelRepository.findById(fuel.getId())).thenReturn(Optional.of(fuel));

        assertThatThrownBy(() -> sut.execute(station.getId(),
            new SetFuelPriceRequest(fuel.getId(), new BigDecimal("5.89"))))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativo");
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId,
            new SetFuelPriceRequest(UUID.randomUUID(), new BigDecimal("5.89"))))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
