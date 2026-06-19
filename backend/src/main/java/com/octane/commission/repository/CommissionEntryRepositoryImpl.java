package com.octane.commission.repository;

import com.octane.commission.domain.CommissionEntry;
import com.octane.commission.domain.repository.CommissionEntryRepository;
import com.octane.shared.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public class CommissionEntryRepositoryImpl implements CommissionEntryRepository {

    private final CommissionEntryJpaRepository jpaRepository;

    public CommissionEntryRepositoryImpl(CommissionEntryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public CommissionEntry save(CommissionEntry entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public Optional<CommissionEntry> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<CommissionEntry> findByShiftId(UUID shiftId) {
        return jpaRepository.findByShift_Id(shiftId);
    }

    @Override
    public PageResponse<CommissionEntry> findByStationId(UUID stationId, Boolean paid,
                                                          LocalDate from, LocalDate to,
                                                          int page, int size) {
        Specification<CommissionEntry> spec =
                (root, query, cb) -> cb.equal(root.get("station").get("id"), stationId);

        if (paid != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("paid"), paid));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) ->
                    cb.greaterThanOrEqualTo(root.get("createdAt"), from.atStartOfDay()));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) ->
                    cb.lessThan(root.get("createdAt"), to.plusDays(1).atStartOfDay()));
        }
        Page<CommissionEntry> result = jpaRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }
}
