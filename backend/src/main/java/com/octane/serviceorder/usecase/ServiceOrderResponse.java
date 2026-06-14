package com.octane.serviceorder.usecase;

import com.octane.serviceorder.domain.ServiceOrder;
import com.octane.serviceorder.domain.ServiceOrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ServiceOrderResponse(
        UUID id,
        UUID stationId,
        String stationName,
        String plate,
        Integer odometer,
        String customerName,
        String customerPhone,
        String status,
        String notes,
        List<ServiceOrderItemResponse> items,
        BigDecimal totalAmount,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt
) {
    public static ServiceOrderResponse from(ServiceOrder order, List<ServiceOrderItem> items) {
        List<ServiceOrderItemResponse> itemResponses = items.stream()
                .map(ServiceOrderItemResponse::from)
                .toList();

        BigDecimal totalAmount = itemResponses.stream()
                .map(ServiceOrderItemResponse::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ServiceOrderResponse(
                order.getId(),
                order.getStation().getId(),
                order.getStation().getName(),
                order.getPlate(),
                order.getOdometer(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getStatus().name(),
                order.getNotes(),
                itemResponses,
                totalAmount,
                order.getOpenedAt(),
                order.getClosedAt(),
                order.getCancelledAt(),
                order.getCreatedAt()
        );
    }
}
