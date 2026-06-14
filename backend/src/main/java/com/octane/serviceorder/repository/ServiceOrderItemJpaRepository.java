package com.octane.serviceorder.repository;

import com.octane.serviceorder.domain.ServiceOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface ServiceOrderItemJpaRepository extends JpaRepository<ServiceOrderItem, UUID> {
    List<ServiceOrderItem> findByServiceOrder_Id(UUID serviceOrderId);
    void deleteByServiceOrder_Id(UUID serviceOrderId);
}
