package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;

    public FindServiceOrderUseCase(ServiceOrderRepository serviceOrderRepository,
                                   ServiceOrderItemRepository serviceOrderItemRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceOrderItemRepository = serviceOrderItemRepository;
    }

    public ServiceOrderResponse execute(UUID orderId) {
        var order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada: " + orderId));

        var items = serviceOrderItemRepository.findByServiceOrderId(orderId);
        return ServiceOrderResponse.from(order, items);
    }
}
