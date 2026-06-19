package com.octane.serviceorder.domain.repository;

import com.octane.serviceorder.domain.ServiceOrderItem;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ServiceOrderItemRepository {
    ServiceOrderItem save(ServiceOrderItem item);
    List<ServiceOrderItem> findByServiceOrderId(UUID serviceOrderId);
    Map<UUID, List<ServiceOrderItem>> findByServiceOrderIds(List<UUID> serviceOrderIds);
    void deleteByServiceOrderId(UUID serviceOrderId);
}
