package com.octane.commission.usecase.rule;

import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteCommissionRuleUseCase {

    private final CommissionRuleRepository commissionRuleRepository;

    public DeleteCommissionRuleUseCase(CommissionRuleRepository commissionRuleRepository) {
        this.commissionRuleRepository = commissionRuleRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var rule = commissionRuleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Regra de comissão não encontrada"));
        rule.setDeletedAt(LocalDateTime.now());
        commissionRuleRepository.save(rule);
    }
}
