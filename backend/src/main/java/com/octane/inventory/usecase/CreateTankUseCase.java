package com.octane.inventory.usecase;

import com.octane.audit.usecase.AuditService;
import com.octane.inventory.domain.Tank;
import com.octane.inventory.domain.repository.TankRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.StationRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateTankUseCase {

    private final TankRepository tankRepository;
    private final StationRepository stationRepository;
    private final FuelRepository fuelRepository;
    private final AuditService auditService;

    public CreateTankUseCase(TankRepository tankRepository,
                              StationRepository stationRepository,
                              FuelRepository fuelRepository,
                              AuditService auditService) {
        this.tankRepository = tankRepository;
        this.stationRepository = stationRepository;
        this.fuelRepository = fuelRepository;
        this.auditService = auditService;
    }

    public record Request(
        @NotNull UUID stationId, @NotNull UUID fuelId, @NotBlank String name,
        @NotNull BigDecimal capacity, @NotNull BigDecimal minimumLevel
    ) {}

    @Transactional
    public TankResponse execute(@Valid Request request) {
        var station = stationRepository.findById(request.stationId())
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado"));
        var fuel = fuelRepository.findById(request.fuelId())
            .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado"));

        var tank = new Tank();
        tank.setStation(station);
        tank.setFuel(fuel);
        tank.setName(request.name());
        tank.setCapacity(request.capacity());
        tank.setCurrentLevel(BigDecimal.ZERO);
        tank.setMinimumLevel(request.minimumLevel());
        tank.setActive(true);
        tank.setCreatedAt(LocalDateTime.now());
        var saved = tankRepository.save(tank);
        auditService.log("CREATE", "Tank", saved.getId(), saved.getName());
        return TankResponse.from(saved);
    }
}
