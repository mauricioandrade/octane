package com.octane.commission.repository;

import com.octane.commission.domain.CommissionRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface CommissionRuleJpaRepository extends JpaRepository<CommissionRule, UUID> {
    List<CommissionRule> findByStation_Id(UUID stationId);
    List<CommissionRule> findByStation_IdAndActive(UUID stationId, boolean active);
    Optional<CommissionRule> findByEmployeeNameAndStation_Id(String employeeName, UUID stationId);
}
