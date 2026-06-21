package com.octane.fleet.usecase;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.FleetVehicle;
import com.octane.fleet.domain.repository.FleetVehicleRepository;
import com.octane.fleet.usecase.vehicle.DeleteFleetVehicleUseCase;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.Station;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteFleetVehicleUseCaseTest {

    @Mock
    private FleetVehicleRepository fleetVehicleRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DeleteFleetVehicleUseCase sut;

    @Test
    void execute_setsDeletedAt() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var station = new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP", true, now, now);
        var client = new FleetClient(UUID.randomUUID(), station, "00.000.000/0001-00", "E", null, BigDecimal.ZERO, true, now);
        var fuel = new Fuel(UUID.randomUUID(), "Gas", FuelUnit.LITER, true, now);
        var vehicle = new FleetVehicle(id, client, "ABC-1234", "Modelo", fuel, true, now);

        when(fleetVehicleRepository.findById(id)).thenReturn(Optional.of(vehicle));
        when(fleetVehicleRepository.save(vehicle)).thenReturn(vehicle);

        sut.execute(id);

        assertThat(vehicle.getDeletedAt()).isNotNull();
        verify(fleetVehicleRepository).save(vehicle);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();
        when(fleetVehicleRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.execute(id)).isInstanceOf(EntityNotFoundException.class);
    }
}
