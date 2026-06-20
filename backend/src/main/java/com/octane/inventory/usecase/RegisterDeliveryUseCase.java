package com.octane.inventory.usecase;

import com.octane.inventory.domain.TankMovement;
import com.octane.inventory.domain.TankMovementType;
import com.octane.inventory.domain.repository.TankRepository;
import com.octane.shared.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterDeliveryUseCase {

    private final TankRepository tankRepository;

    public RegisterDeliveryUseCase(TankRepository tankRepository) {
        this.tankRepository = tankRepository;
    }

    public record Request(@NotNull BigDecimal volumeLiters, String notes) {}

    @Transactional
    public TankResponse execute(UUID tankId, @Valid Request request) {
        var tank = tankRepository.findById(tankId)
            .orElseThrow(() -> new EntityNotFoundException("Tanque não encontrado"));

        var previousLevel = tank.getCurrentLevel();
        var newLevel = previousLevel.add(request.volumeLiters());
        tank.setCurrentLevel(newLevel);
        tankRepository.save(tank);

        var movement = new TankMovement();
        movement.setTank(tank);
        movement.setType(TankMovementType.DELIVERY);
        movement.setVolumeLiters(request.volumeLiters());
        movement.setPreviousLevel(previousLevel);
        movement.setNewLevel(newLevel);
        movement.setNotes(request.notes());
        movement.setCreatedAt(LocalDateTime.now());
        tankRepository.saveMovement(movement);

        return TankResponse.from(tank);
    }
}
