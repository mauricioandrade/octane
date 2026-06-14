package com.octane.serviceorder.repository;

import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.ServiceOrderStatus;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ServiceOrderRepositoryImpl implements ServiceOrderRepository {

    private final ServiceOrderJpaRepository jpaRepository;

    public ServiceOrderRepositoryImpl(ServiceOrderJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ServiceOrder save(ServiceOrder order) {
        return jpaRepository.save(order);
    }

    @Override
    public Optional<ServiceOrder> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ServiceOrder> findByStationId(UUID stationId, ServiceOrderStatus status, LocalDate from, LocalDate to) {
        Specification<ServiceOrder> spec = (root, query, cb) -> cb.equal(root.get("station").get("id"), stationId);
        if (status != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
        }
        if (from != null) {
            spec = spec.and((root, query, cb) ->
                cb.greaterThanOrEqualTo(root.get("openedAt"), from.atStartOfDay()));
        }
        if (to != null) {
            spec = spec.and((root, query, cb) ->
                cb.lessThan(root.get("openedAt"), to.plusDays(1).atStartOfDay()));
        }
        return jpaRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "openedAt"));
    }

    @Override
    public List<ServiceOrder> findByPlate(String plate) {
        return jpaRepository.findByPlateOrderByOpenedAtDesc(plate);
    }
}
