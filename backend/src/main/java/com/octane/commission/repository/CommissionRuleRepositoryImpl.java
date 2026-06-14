package com.octane.commission.repository;

import com.octane.commission.domain.CommissionRule;
import com.octane.commission.domain.repository.CommissionRuleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommissionRuleRepositoryImpl implements CommissionRuleRepository {

    private final CommissionRuleJpaRepository jpaRepository;

    public CommissionRuleRepositoryImpl(CommissionRuleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CommissionRule save(CommissionRule rule) {
        return jpaRepository.save(rule);
    }

    @Override
    public Optional<CommissionRule> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<CommissionRule> findByStationId(UUID stationId, Boolean active) {
        if (active != null) {
            return jpaRepository.findByStation_IdAndActive(stationId, active);
        }
        return jpaRepository.findByStation_Id(stationId);
    }

    @Override
    public Optional<CommissionRule> findByEmployeeNameAndStationId(String employeeName, UUID stationId) {
        return jpaRepository.findByEmployeeNameAndStation_Id(employeeName, stationId);
    }
}
