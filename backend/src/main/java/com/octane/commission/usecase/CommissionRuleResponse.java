package com.octane.commission.usecase;

import com.octane.commission.domain.CommissionRule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CommissionRuleResponse(
        UUID id,
        UUID stationId,
        String employeeName,
        BigDecimal rate,
        boolean active,
        LocalDateTime createdAt
) {
    public static CommissionRuleResponse from(CommissionRule rule) {
        return new CommissionRuleResponse(
                rule.getId(),
                rule.getStation().getId(),
                rule.getEmployeeName(),
                rule.getRate(),
                rule.isActive(),
                rule.getCreatedAt()
        );
    }
}
