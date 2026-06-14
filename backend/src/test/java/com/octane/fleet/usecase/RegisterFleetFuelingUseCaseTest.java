package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.FleetDriver;
import com.octane.fleet.domain.FleetFueling;
import com.octane.fleet.domain.FleetVehicle;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.domain.repository.FleetFuelingRepository;
import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.fleet.usecase.fueling.RegisterFleetFuelingRequest;
import com.octane.fleet.usecase.fueling.RegisterFleetFuelingUseCase;
import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.usecase.fueling.RegisterFuelingUseCase;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegisterFleetFuelingUseCaseTest {

    @Mock
    private FleetDriverRepository fleetDriverRepository;

    @Mock
    private FleetVehicleRepository fleetVehicleRepository;

    @Mock
    private FleetClientRepository fleetClientRepository;

    @Mock
    private FleetFuelingRepository fleetFuelingRepository;

    @Mock
    private NozzleRepository nozzleRepository;

    @Mock
    private RegisterFuelingUseCase registerFuelingUseCase;

    @InjectMocks
    private RegisterFleetFuelingUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());
    private final FleetClient client = new FleetClient(UUID.randomUUID(), station, "12.345.678/0001-90",
            "Empresa", null, null, true, LocalDateTime.now());
    private final Fuel fuel = new Fuel(UUID.randomUUID(), "Gasolina Comum", FuelUnit.LITER,
            true, LocalDateTime.now());
    private final FleetDriver driver = new FleetDriver(UUID.randomUUID(), client, "João",
            "123.456.789-09", "hash", null, true, LocalDateTime.now());
    private final FleetVehicle vehicle = new FleetVehicle(UUID.randomUUID(), client, "ABC1234",
            "Fiat Uno", fuel, true, LocalDateTime.now());
    private final Pump pump = new Pump(UUID.randomUUID(), 1, PumpStatus.ACTIVE, station,
            LocalDateTime.now(), LocalDateTime.now());
    private final Nozzle nozzle = new Nozzle(UUID.randomUUID(), 1, pump, fuel, true,
            LocalDateTime.now(), LocalDateTime.now());

    private final Shift shift = new Shift(UUID.randomUUID(), station, "Funcionário",
            ShiftStatus.OPEN, LocalDateTime.now(), null, null, LocalDateTime.now());

    private void stubHappyPath() {
        when(fleetDriverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        when(fleetVehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(nozzleRepository.findById(nozzle.getId())).thenReturn(Optional.of(nozzle));
    }

    private Fueling buildFueling() {
        return new Fueling(UUID.randomUUID(), shift, nozzle,
                new BigDecimal("10.000"), new BigDecimal("5.00"), new BigDecimal("50.00"),
                PaymentMethod.PIX, FuelingStatus.ACTIVE, null, "ABC1234", null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void execute_registersFleetFueling_happyPath() {
        stubHappyPath();
        var fueling = buildFueling();
        when(registerFuelingUseCase.execute(eq(shift.getId()), any())).thenReturn(fueling);
        when(fleetFuelingRepository.findLastOdometerByVehicleId(vehicle.getId())).thenReturn(Optional.of(10000));
        when(fleetFuelingRepository.save(any(FleetFueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new RegisterFleetFuelingRequest(shift.getId(), nozzle.getId(),
                new BigDecimal("10.000"), new BigDecimal("50.00"), "PIX",
                driver.getId(), vehicle.getId(), 11000, null);

        var result = sut.execute(request);

        assertThat(result.odometerAlert()).isFalse();
        assertThat(result.odometer()).isEqualTo(11000);
        assertThat(result.previousOdometer()).isEqualTo(10000);
        verify(fleetFuelingRepository).save(any(FleetFueling.class));
    }

    @Test
    void execute_setsOdometerAlert_whenOdometerDecreases() {
        stubHappyPath();
        var fueling = buildFueling();
        when(registerFuelingUseCase.execute(eq(shift.getId()), any())).thenReturn(fueling);
        when(fleetFuelingRepository.findLastOdometerByVehicleId(vehicle.getId())).thenReturn(Optional.of(15000));
        when(fleetFuelingRepository.save(any(FleetFueling.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new RegisterFleetFuelingRequest(shift.getId(), nozzle.getId(),
                new BigDecimal("10.000"), new BigDecimal("50.00"), "PIX",
                driver.getId(), vehicle.getId(), 12000, null);

        var result = sut.execute(request);

        assertThat(result.odometerAlert()).isTrue();
    }

    @Test
    void execute_throwsBusinessException_whenVehicleDoesNotBelongToDriversClient() {
        var otherClient = new FleetClient(UUID.randomUUID(), station, "99.999.999/0001-99",
                "Outra Empresa", null, null, true, LocalDateTime.now());
        var vehicleOfOtherClient = new FleetVehicle(UUID.randomUUID(), otherClient, "XYZ9999",
                "VW Gol", fuel, true, LocalDateTime.now());

        when(fleetDriverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        when(fleetVehicleRepository.findById(vehicleOfOtherClient.getId())).thenReturn(Optional.of(vehicleOfOtherClient));

        var request = new RegisterFleetFuelingRequest(shift.getId(), nozzle.getId(),
                null, new BigDecimal("50.00"), "PIX",
                driver.getId(), vehicleOfOtherClient.getId(), 11000, null);

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("cliente");

        verify(registerFuelingUseCase, never()).execute(any(), any());
    }

    @Test
    void execute_throwsBusinessException_whenFuelNotAllowed() {
        var otherFuel = new Fuel(UUID.randomUUID(), "Diesel S500", FuelUnit.LITER, true, LocalDateTime.now());
        var nozzleWithOtherFuel = new Nozzle(UUID.randomUUID(), 2, pump, otherFuel, true,
                LocalDateTime.now(), LocalDateTime.now());

        when(fleetDriverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        when(fleetVehicleRepository.findById(vehicle.getId())).thenReturn(Optional.of(vehicle));
        when(nozzleRepository.findById(nozzleWithOtherFuel.getId())).thenReturn(Optional.of(nozzleWithOtherFuel));

        var request = new RegisterFleetFuelingRequest(shift.getId(), nozzleWithOtherFuel.getId(),
                null, new BigDecimal("50.00"), "PIX",
                driver.getId(), vehicle.getId(), 11000, null);

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Combustível não permitido");

        verify(registerFuelingUseCase, never()).execute(any(), any());
    }

    @Test
    void execute_throwsEntityNotFoundException_whenDriverNotFound() {
        var driverId = UUID.randomUUID();
        when(fleetDriverRepository.findById(driverId)).thenReturn(Optional.empty());

        var request = new RegisterFleetFuelingRequest(shift.getId(), nozzle.getId(),
                null, new BigDecimal("50.00"), "PIX",
                driverId, vehicle.getId(), 11000, null);

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
