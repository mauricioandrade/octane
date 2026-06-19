package com.octane.commission.repository;

import com.octane.commission.domain.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

interface CommissionRuleJpaRepository extends JpaRepository<CommissionRule, UUID>,
        JpaSpecificationExecutor<CommissionRule> {
    Optional<CommissionRule> findByEmployeeNameAndStation_Id(String employeeName, UUID stationId);
}
