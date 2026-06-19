package com.octane.commission.usecase.entry;

import com.octane.commission.domain.CommissionEntry;
import com.octane.commission.domain.repository.CommissionEntryRepository;
import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class CalculateCommissionUseCase {

    private final ShiftRepository shiftRepository;
    private final FuelingRepository fuelingRepository;
    private final CommissionRuleRepository commissionRuleRepository;
    private final CommissionEntryRepository commissionEntryRepository;

    public CalculateCommissionUseCase(ShiftRepository shiftRepository,
                                      FuelingRepository fuelingRepository,
                                      CommissionRuleRepository commissionRuleRepository,
                                      CommissionEntryRepository commissionEntryRepository) {
        this.shiftRepository = shiftRepository;
        this.fuelingRepository = fuelingRepository;
        this.commissionRuleRepository = commissionRuleRepository;
        this.commissionEntryRepository = commissionEntryRepository;
    }

    @Transactional
    public Optional<CommissionEntry> execute(UUID shiftId) {
        var shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Turno não encontrado: " + shiftId));

        if (shift.getStatus() != ShiftStatus.CLOSED) {
            throw new BusinessException("Turno não está fechado");
        }

        if (commissionEntryRepository.findByShiftId(shiftId).isPresent()) {
            throw new BusinessException("Comissão já calculada para este turno");
        }

        var ruleOpt = commissionRuleRepository.findByEmployeeNameAndStationId(
                shift.getEmployeeName(), shift.getStation().getId())
                .filter(r -> r.isActive());

        if (ruleOpt.isEmpty()) {
            return Optional.empty();
        }

        var rule = ruleOpt.get();

        var fuelings = fuelingRepository.findByShiftId(shiftId);
        var baseAmount = fuelings.stream()
                .filter(f -> f.getStatus() == FuelingStatus.ACTIVE)
                .map(f -> f.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        var commission = baseAmount.multiply(rule.getRate()).setScale(2, RoundingMode.HALF_UP);

        var now = LocalDateTime.now();
        var entry = new CommissionEntry(
                null, shift, shift.getEmployeeName(), shift.getStation(),
                baseAmount, rule.getRate(), commission, false, null, now
        );

        return Optional.of(commissionEntryRepository.save(entry));
    }
}
