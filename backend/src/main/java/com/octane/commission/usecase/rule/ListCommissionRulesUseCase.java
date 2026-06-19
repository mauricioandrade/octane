package com.octane.commission.usecase.rule;

import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.commission.usecase.CommissionRuleResponse;
import com.octane.shared.pagination.PageResponse;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ListCommissionRulesUseCase {

    private final CommissionRuleRepository commissionRuleRepository;

    public ListCommissionRulesUseCase(CommissionRuleRepository commissionRuleRepository) {
        this.commissionRuleRepository = commissionRuleRepository;
    }

    public PageResponse<CommissionRuleResponse> execute(UUID stationId, Boolean active, int page, int size) {
        return commissionRuleRepository.findByStationId(stationId, active, page, size)
                .map(CommissionRuleResponse::from);
    }
}
