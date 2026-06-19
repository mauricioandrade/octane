package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftReconciliationRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class GetShiftReconciliationUseCase {

    private final ShiftRepository shiftRepository;
    private final ShiftReconciliationRepository shiftReconciliationRepository;

    public GetShiftReconciliationUseCase(ShiftRepository shiftRepository,
                                         ShiftReconciliationRepository shiftReconciliationRepository) {
        this.shiftRepository = shiftRepository;
        this.shiftReconciliationRepository = shiftReconciliationRepository;
    }

    public ShiftReconciliationResponse execute(UUID shiftId) {
        var shift = shiftRepository.findById(shiftId)
            .orElseThrow(() -> new EntityNotFoundException("Turno não encontrado: " + shiftId));

        if (shift.getStatus() != ShiftStatus.CLOSED) {
            throw new BusinessException("Conciliação disponível apenas para turno fechado");
        }

        var reconciliations = shiftReconciliationRepository.findByShiftId(shiftId);

        var lines = reconciliations.stream().map(ReconciliationLineResponse::from).toList();
        var totalMeasured = reconciliations.stream()
            .map(r -> r.getMeasuredLiters()).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalFueled = reconciliations.stream()
            .map(r -> r.getFueledLiters()).reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalDivergence = reconciliations.stream()
            .map(r -> r.getDivergenceLiters()).reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShiftReconciliationResponse(shiftId, lines, totalMeasured, totalFueled, totalDivergence);
    }
}
