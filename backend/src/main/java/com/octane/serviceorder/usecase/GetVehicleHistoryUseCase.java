package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        return orders.stream()
                .map(order -> ServiceOrderResponse.from(order,
                        serviceOrderItemRepository.findByServiceOrderId(order.getId())))
                .toList();
    }
}
