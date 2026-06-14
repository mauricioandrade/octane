package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrderItem;
import com.octane.serviceorder.domain.ServiceOrderStatus;
import com.octane.serviceorder.domain.repository.ServiceOrderItemRepository;
import com.octane.serviceorder.domain.repository.ServiceOrderRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AddServiceOrderItemUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;

    public AddServiceOrderItemUseCase(ServiceOrderRepository serviceOrderRepository,
                                      ServiceOrderItemRepository serviceOrderItemRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceOrderItemRepository = serviceOrderItemRepository;
    }

    @Transactional
    public ServiceOrderResponse execute(UUID orderId, AddServiceOrderItemRequest request) {
        var order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada: " + orderId));

        if (order.getStatus() != ServiceOrderStatus.OPEN) {
            throw new BusinessException("OS não está aberta");
        }

        var item = new ServiceOrderItem(null, order, request.description(),
                request.quantity(), request.unitPrice(), LocalDateTime.now());
        serviceOrderItemRepository.save(item);

        var items = serviceOrderItemRepository.findByServiceOrderId(orderId);
        return ServiceOrderResponse.from(order, items);
    }
}
