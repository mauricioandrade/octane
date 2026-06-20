package com.octane.financial.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cash_movements")
public class CashMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "cash_register_id", nullable = false)
    private CashRegister cashRegister;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MovementType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MovementCategory category;

    @Column(length = 200)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_method", length = 20)
    private String paymentMethod;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CashMovement() {}

    public UUID getId() { return id; }
    public CashRegister getCashRegister() { return cashRegister; }
    public MovementType getType() { return type; }
    public MovementCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public UUID getReferenceId() { return referenceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCashRegister(CashRegister cashRegister) { this.cashRegister = cashRegister; }
    public void setType(MovementType type) { this.type = type; }
    public void setCategory(MovementCategory category) { this.category = category; }
    public void setDescription(String description) { this.description = description; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
