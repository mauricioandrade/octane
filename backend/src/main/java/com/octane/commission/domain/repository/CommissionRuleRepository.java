package com.octane.commission.domain.repository;

import com.octane.commission.domain.CommissionRule;
import com.octane.shared.pagination.PageResponse;

import java.util.Optional;
import java.util.UUID;

public interface CommissionRuleRepository {
    CommissionRule save(CommissionRule rule);
    Optional<CommissionRule> findById(UUID id);
    PageResponse<CommissionRule> findByStationId(UUID stationId, Boolean active, int page, int size);
    Optional<CommissionRule> findByEmployeeNameAndStationId(String employeeName, UUID stationId);
}
