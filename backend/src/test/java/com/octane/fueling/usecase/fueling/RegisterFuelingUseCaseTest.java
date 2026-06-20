package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.inventory.domain.repository.TankRepository;
import com.octane.pricing.domain.FuelPrice;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.BusinessException;
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
class RegisterFuelingUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private FuelingRepository fuelingRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @Mock
    private FuelPriceRepository fuelPriceRepository;

    @Mock
    private TankRepository tankRepository;

    @InjectMocks
    private RegisterFuelingUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
        "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
        LocalDateTime.now(), LocalDateTime.now());
    private final Fuel fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
        true, LocalDateTime.now());
    private final Nozzle nozzle = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true,
        LocalDateTime.now(), LocalDateTime.now());
    private final Shift openShift = new Shift(UUID.randomUUID(), station, "João",
        ShiftStatus.OPEN, LocalDateTime.now(), null, null, LocalDateTime.now());
    private final FuelPrice currentPrice = new FuelPrice(UUID.randomUUID(), station, fuel,
        new BigDecimal("5.00"), LocalDateTime.now(), LocalDateTime.now());

    private void stubHappyPath() {
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(nozzle.getId())).thenReturn(Optional.of(nozzle));
        when(fuelPriceRepository.findCurrent(station.getId(), fuel.getId()))
            .thenReturn(Optional.of(currentPrice));
    }

    @Test
    void execute_computesTotalFromLiters_usingCurrentPrice() {
        stubHappyPath();
        when(fuelingRepository.save(any(Fueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        var result = sut.execute(openShift.getId(), request);

        assertThat(result.getLiters()).isEqualByComparingTo("10.000");
        assertThat(result.getUnitPrice()).isEqualByComparingTo("5.00");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void execute_computesLitersFromTotal_usingCurrentPrice() {
        stubHappyPath();
        when(fuelingRepository.save(any(Fueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new RegisterFuelingRequest(nozzle.getId(), null,
            new BigDecimal("50.00"), "CASH", null, null);

        var result = sut.execute(openShift.getId(), request);

        assertThat(result.getLiters()).isEqualByComparingTo("10.000");
        assertThat(result.getTotalAmount()).isEqualByComparingTo("50.00");
    }

    @Test
    void execute_acceptsBothValues_whenConsistentWithinTolerance() {
        stubHappyPath();
        when(fuelingRepository.save(any(Fueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            new BigDecimal("50.01"), "PIX", null, null);

        var result = sut.execute(openShift.getId(), request);

        assertThat(result.getTotalAmount()).isEqualByComparingTo("50.01");
    }

    @Test
    void execute_throwsBusinessException_whenBothValuesInconsistent() {
        stubHappyPath();

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            new BigDecimal("55.00"), "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("não confere");

        verify(fuelingRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenNeitherLitersNorTotalGiven() {
        stubHappyPath();

        var request = new RegisterFuelingRequest(nozzle.getId(), null, null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("litros ou valor total");
    }

    @Test
    void execute_throwsBusinessException_whenNoPriceRegistered() {
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(nozzle.getId())).thenReturn(Optional.of(nozzle));
        when(fuelPriceRepository.findCurrent(station.getId(), fuel.getId()))
            .thenReturn(Optional.empty());

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("preço");
    }

    @Test
    void execute_throwsBusinessException_whenNozzleInactive() {
        var inactiveNozzle = new Nozzle(UUID.randomUUID(), 2, pump, fuel, false,
            LocalDateTime.now(), LocalDateTime.now());
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(inactiveNozzle.getId())).thenReturn(Optional.of(inactiveNozzle));

        var request = new RegisterFuelingRequest(inactiveNozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("inativ");
    }

    @Test
    void execute_throwsBusinessException_whenPumpNotActive() {
        var maintenancePump = new Pump(UUID.randomUUID(), 2, PumpStatus.MAINTENANCE, station,
            LocalDateTime.now(), LocalDateTime.now());
        var nozzleOnMaintenancePump = new Nozzle(UUID.randomUUID(), 1, maintenancePump, fuel, true,
            LocalDateTime.now(), LocalDateTime.now());
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(nozzleOnMaintenancePump.getId()))
            .thenReturn(Optional.of(nozzleOnMaintenancePump));

        var request = new RegisterFuelingRequest(nozzleOnMaintenancePump.getId(),
            new BigDecimal("10.000"), null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("Bomba");
    }

    @Test
    void execute_throwsBusinessException_whenShiftNotOpen() {
        var closedShift = new Shift(UUID.randomUUID(), station, "João", ShiftStatus.CLOSED,
            LocalDateTime.now(), LocalDateTime.now(), null, LocalDateTime.now());
        when(shiftRepository.findById(closedShift.getId())).thenReturn(Optional.of(closedShift));

        var request = new RegisterFuelingRequest(nozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(closedShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("aberto");
    }

    @Test
    void execute_throwsBusinessException_whenNozzleBelongsToAnotherStation() {
        var otherStation = new Station(UUID.randomUUID(), "Posto Y", "99.999.999/0001-99",
            "Rua B, 2", "Campinas", "SP", true, LocalDateTime.now(), LocalDateTime.now());
        var otherPump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, otherStation,
            LocalDateTime.now(), LocalDateTime.now());
        var foreignNozzle = new Nozzle(UUID.randomUUID(), 1, otherPump, fuel, true,
            LocalDateTime.now(), LocalDateTime.now());
        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));
        when(nozzleRepository.findById(foreignNozzle.getId())).thenReturn(Optional.of(foreignNozzle));

        var request = new RegisterFuelingRequest(foreignNozzle.getId(), new BigDecimal("10.000"),
            null, "PIX", null, null);

        assertThatThrownBy(() -> sut.execute(openShift.getId(), request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("posto");
    }

    @Test
    void execute_throwsEntityNotFound_whenShiftMissing() {
        var shiftId = UUID.randomUUID();
        when(shiftRepository.findById(shiftId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(shiftId,
            new RegisterFuelingRequest(nozzle.getId(), BigDecimal.ONE, null, "PIX", null, null)))
            .isInstanceOf(EntityNotFoundException.class);
    }
}
