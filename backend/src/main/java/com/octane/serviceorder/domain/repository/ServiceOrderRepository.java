package com.octane.serviceorder.domain.repository;

import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.ServiceOrderStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ServiceOrderRepository {
    ServiceOrder save(ServiceOrder order);
    Optional<ServiceOrder> findById(UUID id);
    List<ServiceOrder> findByStationId(UUID stationId, ServiceOrderStatus status, LocalDate from, LocalDate to);
    List<ServiceOrder> findByPlate(String plate);
}
