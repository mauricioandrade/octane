package com.octane.commission.usecase.rule;

import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.commission.usecase.CommissionRuleResponse;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateCommissionRuleUseCase {

    private final CommissionRuleRepository commissionRuleRepository;

    public UpdateCommissionRuleUseCase(CommissionRuleRepository commissionRuleRepository) {
        this.commissionRuleRepository = commissionRuleRepository;
    }

    @Transactional
    public CommissionRuleResponse execute(UUID id, UpdateCommissionRuleRequest request) {
        var rule = commissionRuleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("CommissionRule not found: " + id));

        if (request.employeeName() != null) {
            rule.setEmployeeName(request.employeeName());
        }
        if (request.rate() != null) {
            rule.setRate(request.rate());
        }
        if (request.active() != null) {
            rule.setActive(request.active());
        }

        var saved = commissionRuleRepository.save(rule);
        return CommissionRuleResponse.from(saved);
    }
}
