package com.octane.fueling.repository;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.pagination.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ShiftRepositoryImpl implements ShiftRepository {

    private final ShiftJpaRepository jpaRepository;

    public ShiftRepositoryImpl(ShiftJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Shift save(Shift shift) {
        return jpaRepository.save(shift);
    }

    @Override
    public Optional<Shift> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Shift> findOpenByStationId(UUID stationId) {
        return jpaRepository.findByStation_IdAndStatus(stationId, ShiftStatus.OPEN);
    }

    @Override
    public PageResponse<Shift> findByStationId(UUID stationId, ShiftStatus status,
                                               LocalDateTime from, LocalDateTime to,
                                               int page, int size) {
        Specification<Shift> spec = (root, query, cb) -> cb.equal(root.get("station").get("id"), stationId);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("openedAt"), from));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("openedAt"), to));
        }
        Page<Shift> result = jpaRepository.findAll(spec,
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "openedAt")));
        return PageResponse.of(result.getContent(), page, size, result.getTotalElements());
    }
}
