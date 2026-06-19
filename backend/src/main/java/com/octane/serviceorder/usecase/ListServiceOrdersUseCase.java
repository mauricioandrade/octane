package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.ServiceOrderStatus;
import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListServiceOrdersUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;

    public ListServiceOrdersUseCase(ServiceOrderRepository serviceOrderRepository,
                                    ServiceOrderItemRepository serviceOrderItemRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceOrderItemRepository = serviceOrderItemRepository;
    }

    public List<ServiceOrderResponse> execute(UUID stationId, String status, LocalDate from, LocalDate to) {
        ServiceOrderStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = ServiceOrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new com.octane.shared.exception.BusinessException("Status inválido: " + status);
            }
        }
        var orders = serviceOrderRepository.findByStationId(stationId, statusEnum, from, to);
        var itemsByOrder = serviceOrderItemRepository.findByServiceOrderIds(
                orders.stream().map(ServiceOrder::getId).toList());
        return orders.stream()
                .map(order -> ServiceOrderResponse.from(order,
                        itemsByOrder.getOrDefault(order.getId(), Collections.emptyList())))
                .toList();
    }
}
