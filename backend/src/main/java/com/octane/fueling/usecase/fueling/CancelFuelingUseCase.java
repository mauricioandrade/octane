package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CancelFuelingUseCase {

    private final FuelingRepository fuelingRepository;

    public CancelFuelingUseCase(FuelingRepository fuelingRepository) {
        this.fuelingRepository = fuelingRepository;
    }

    @Transactional
    public Fueling execute(UUID shiftId, UUID fuelingId) {
        var fueling = fuelingRepository.findById(fuelingId)
            .orElseThrow(() -> new EntityNotFoundException("Abastecimento não encontrado: " + fuelingId));

        if (!fueling.getShift().getId().equals(shiftId)) {
            throw new EntityNotFoundException("Abastecimento não encontrado no turno: " + fuelingId);
        }

        if (fueling.getStatus() == FuelingStatus.CANCELED) {
            throw new BusinessException("Abastecimento já cancelado");
        }

        if (fueling.getShift().getStatus() != ShiftStatus.OPEN) {
            throw new BusinessException("Turno não está aberto: não é possível cancelar");
        }

        var canceled = new Fueling(fueling.getId(), fueling.getShift(), fueling.getNozzle(),
            fueling.getLiters(), fueling.getUnitPrice(), fueling.getTotalAmount(),
            fueling.getPaymentMethod(), FuelingStatus.CANCELED, LocalDateTime.now(),
            fueling.getVehiclePlate(), fueling.getNotes(), fueling.getFueledAt(),
            fueling.getCreatedAt());
        return fuelingRepository.save(canceled);
    }
}
