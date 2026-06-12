package com.octane.pricing.usecase;

import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCurrentPricesUseCaseTest {

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @Mock
    private StationRepository stationRepository;

    @InjectMocks
    private GetCurrentPricesUseCase sut;

    @Test
    void execute_returnsCurrentPrices() {
        var station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90", "Rua A, 1",
            "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var fuel = new Fuel(UUID.randomUUID(), "Etanol", FuelUnit.LITER, true, LocalDateTime.now());
        var price = new FuelPrice(UUID.randomUUID(), station, fuel, new BigDecimal("3.99"),
            LocalDateTime.now(), LocalDateTime.now());

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(fuelPriceRepository.findCurrentByStation(station.getId())).thenReturn(List.of(price));

        var result = sut.execute(station.getId());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPrice()).isEqualByComparingTo("3.99");
    }

    @Test
    void execute_throwsEntityNotFound_whenStationMissing() {
        var stationId = UUID.randomUUID();
        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(stationId))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
