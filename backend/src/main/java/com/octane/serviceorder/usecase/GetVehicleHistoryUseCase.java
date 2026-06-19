package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GetVehicleHistoryUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;

    public GetVehicleHistoryUseCase(ServiceOrderRepository serviceOrderRepository,
                                    ServiceOrderItemRepository serviceOrderItemRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceOrderItemRepository = serviceOrderItemRepository;
    }

    public List<ServiceOrderResponse> execute(String plate) {
        var orders = serviceOrderRepository.findByPlate(plate);
        var itemsByOrder = serviceOrderItemRepository.findByServiceOrderIds(
                orders.stream().map(ServiceOrder::getId).toList());
        return orders.stream()
                .map(order -> ServiceOrderResponse.from(order,
                        itemsByOrder.getOrDefault(order.getId(), Collections.emptyList())))
                .toList();
    }
}
