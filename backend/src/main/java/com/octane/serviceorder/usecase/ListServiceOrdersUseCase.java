package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrderStatus;
import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
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
        return orders.stream()
                .map(order -> ServiceOrderResponse.from(order,
                        serviceOrderItemRepository.findByServiceOrderId(order.getId())))
                .toList();
    }
}
