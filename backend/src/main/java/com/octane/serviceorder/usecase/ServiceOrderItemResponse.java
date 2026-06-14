package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ServiceOrderItemResponse(
        UUID id,
        String description,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal totalPrice,
        LocalDateTime createdAt
) {
    public static ServiceOrderItemResponse from(ServiceOrderItem item) {
        return new ServiceOrderItemResponse(
                item.getId(),
                item.getDescription(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getTotalPrice(),
                item.getCreatedAt()
        );
    }
}
