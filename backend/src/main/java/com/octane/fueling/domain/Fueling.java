package com.octane.fueling.domain;

import com.octane.station.domain.Nozzle;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fuelings")
public class Fueling {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne
    @JoinColumn(name = "nozzle_id", nullable = false)
    private Nozzle nozzle;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal liters;

    @Column(name = "unit_price", nullable = false, precision = 10, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 30)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuelingStatus status;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "vehicle_plate", length = 10)
    private String vehiclePlate;

    @Column(length = 300)
    private String notes;

    @Column(name = "fueled_at", nullable = false)
    private LocalDateTime fueledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Fueling() {}

    public Fueling(UUID id, Shift shift, Nozzle nozzle, BigDecimal liters, BigDecimal unitPrice,
                   BigDecimal totalAmount, PaymentMethod paymentMethod, FuelingStatus status,
                   LocalDateTime canceledAt, String vehiclePlate, String notes,
                   LocalDateTime fueledAt, LocalDateTime createdAt) {
        this.id = id;
        this.shift = shift;
        this.nozzle = nozzle;
        this.liters = liters;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.canceledAt = canceledAt;
        this.vehiclePlate = vehiclePlate;
        this.notes = notes;
        this.fueledAt = fueledAt;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Shift getShift() { return shift; }
    public Nozzle getNozzle() { return nozzle; }
    public BigDecimal getLiters() { return liters; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public FuelingStatus getStatus() { return status; }
    public LocalDateTime getCanceledAt() { return canceledAt; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getNotes() { return notes; }
    public LocalDateTime getFueledAt() { return fueledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
