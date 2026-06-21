package com.octane.station.usecase.fuel;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.BusinessException;
import com.octane.station.domain.Fuel;
import com.octane.station.domain.FuelUnit;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateFuelUseCase {

    private final FuelRepository fuelRepository;
    private final AuditService auditService;

    public CreateFuelUseCase(FuelRepository fuelRepository, AuditService auditService) {
        this.fuelRepository = fuelRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Fuel execute(CreateFuelRequest request) {
        fuelRepository.findByName(request.name()).ifPresent(existing -> {
            throw new BusinessException("Combustível já cadastrado: " + request.name());
        });

        FuelUnit fuelUnit;
        try {
            fuelUnit = FuelUnit.valueOf(request.unit());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Unidade inválida: " + request.unit());
        }

        var fuel = new Fuel(null, request.name(), fuelUnit, true, LocalDateTime.now());
        var saved = fuelRepository.save(fuel);
        auditService.log("CREATE", "Fuel", saved.getId(), saved.getName());
        return saved;
    }
}
