package com.octane.commission.usecase.rule;

import com.octane.audit.usecase.AuditService;
import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteCommissionRuleUseCase {

    private final CommissionRuleRepository commissionRuleRepository;
    private final AuditService auditService;

    public DeleteCommissionRuleUseCase(CommissionRuleRepository commissionRuleRepository,
                                       AuditService auditService) {
        this.commissionRuleRepository = commissionRuleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public void execute(UUID id) {
        var rule = commissionRuleRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Regra de comissão não encontrada"));
        rule.setDeletedAt(LocalDateTime.now());
        commissionRuleRepository.save(rule);
        auditService.log("DELETE", "CommissionRule", id, null);
    }
}
