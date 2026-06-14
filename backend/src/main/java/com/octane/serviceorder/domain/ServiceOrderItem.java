package com.octane.serviceorder.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_order_items")
public class ServiceOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "service_order_id", nullable = false)
    private ServiceOrder serviceOrder;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(precision = 8, scale = 3, nullable = false)
    private BigDecimal quantity;

    @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
    private BigDecimal unitPrice;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ServiceOrderItem() {}

    public ServiceOrderItem(UUID id, ServiceOrder serviceOrder, String description,
                            BigDecimal quantity, BigDecimal unitPrice, LocalDateTime createdAt) {
        this.id = id;
        this.serviceOrder = serviceOrder;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public ServiceOrder getServiceOrder() { return serviceOrder; }
    public String getDescription() { return description; }
    public BigDecimal getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public BigDecimal getTotalPrice() { return quantity.multiply(unitPrice); }
}
