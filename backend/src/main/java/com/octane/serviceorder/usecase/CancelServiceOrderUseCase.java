package com.octane.serviceorder.usecase;

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
public class CancelServiceOrderUseCase {

    private final ServiceOrderRepository serviceOrderRepository;
    private final ServiceOrderItemRepository serviceOrderItemRepository;

    public CancelServiceOrderUseCase(ServiceOrderRepository serviceOrderRepository,
                                     ServiceOrderItemRepository serviceOrderItemRepository) {
        this.serviceOrderRepository = serviceOrderRepository;
        this.serviceOrderItemRepository = serviceOrderItemRepository;
    }

    @Transactional
    public ServiceOrderResponse execute(UUID orderId) {
        var order = serviceOrderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Ordem de serviço não encontrada: " + orderId));

        if (order.getStatus() != ServiceOrderStatus.OPEN) {
            throw new BusinessException("OS já foi encerrada ou cancelada");
        }

        order.setStatus(ServiceOrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());
        var saved = serviceOrderRepository.save(order);

        var items = serviceOrderItemRepository.findByServiceOrderId(orderId);
        return ServiceOrderResponse.from(saved, items);
    }
}
