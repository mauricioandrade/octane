package com.octane.commission.domain.repository;

import com.octane.commission.domain.CommissionRule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommissionRuleRepository {
    CommissionRule save(CommissionRule rule);
    Optional<CommissionRule> findById(UUID id);
    List<CommissionRule> findByStationId(UUID stationId, Boolean active);
    Optional<CommissionRule> findByEmployeeNameAndStationId(String employeeName, UUID stationId);
}
