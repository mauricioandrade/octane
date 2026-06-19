package com.octane.commission.repository;

import com.octane.commission.domain.CommissionRule;
import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.shared.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

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
    public PageResponse<CommissionRule> findByStationId(UUID stationId, Boolean active, int page, int size) {
        Specification<CommissionRule> spec =
                (root, query, cb) -> cb.equal(root.get("station").get("id"), stationId);
        if (active != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("active"), active));
        }
        Page<CommissionRule> result = jpaRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }

    @Override
    public Optional<CommissionRule> findByEmployeeNameAndStationId(String employeeName, UUID stationId) {
        return jpaRepository.findByEmployeeNameAndStation_Id(employeeName, stationId);
    }
}
