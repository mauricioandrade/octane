package com.octane.serviceorder.domain.repository;

import com.octane.serviceorder.domain.ServiceOrderItem;

import java.util.List;
import java.util.UUID;

public interface ServiceOrderItemRepository {
    ServiceOrderItem save(ServiceOrderItem item);
    List<ServiceOrderItem> findByServiceOrderId(UUID serviceOrderId);
    void deleteByServiceOrderId(UUID serviceOrderId);
}
