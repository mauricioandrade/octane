package com.octane.serviceorder.repository;

import com.octane.serviceorder.domain.ServiceOrderItem;
import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public class ServiceOrderItemRepositoryImpl implements ServiceOrderItemRepository {

    private final ServiceOrderItemJpaRepository jpaRepository;

    public ServiceOrderItemRepositoryImpl(ServiceOrderItemJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ServiceOrderItem save(ServiceOrderItem item) {
        return jpaRepository.save(item);
    }

    @Override
    public List<ServiceOrderItem> findByServiceOrderId(UUID serviceOrderId) {
        return jpaRepository.findByServiceOrder_Id(serviceOrderId);
    }

    @Override
    @Transactional
    public void deleteByServiceOrderId(UUID serviceOrderId) {
        jpaRepository.deleteByServiceOrder_Id(serviceOrderId);
    }
}
