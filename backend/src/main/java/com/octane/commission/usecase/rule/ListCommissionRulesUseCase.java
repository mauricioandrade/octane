package com.octane.commission.usecase.rule;

import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.commission.usecase.CommissionRuleResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListCommissionRulesUseCase {

    private final CommissionRuleRepository commissionRuleRepository;

    public ListCommissionRulesUseCase(CommissionRuleRepository commissionRuleRepository) {
        this.commissionRuleRepository = commissionRuleRepository;
    }

    public List<CommissionRuleResponse> execute(UUID stationId, Boolean active) {
        return commissionRuleRepository.findByStationId(stationId, active)
                .stream()
                .map(CommissionRuleResponse::from)
                .toList();
    }
}
