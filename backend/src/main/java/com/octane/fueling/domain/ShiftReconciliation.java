package com.octane.fueling.domain;

import com.octane.station.domain.Nozzle;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shift_reconciliations")
public class ShiftReconciliation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne
    @JoinColumn(name = "nozzle_id", nullable = false)
    private Nozzle nozzle;

    @Column(name = "opening_totalizer", nullable = false, precision = 12, scale = 3)
    private BigDecimal openingTotalizer;

    @Column(name = "closing_totalizer", nullable = false, precision = 12, scale = 3)
    private BigDecimal closingTotalizer;

    @Column(name = "measured_liters", nullable = false, precision = 12, scale = 3)
    private BigDecimal measuredLiters;

    @Column(name = "fueled_liters", nullable = false, precision = 12, scale = 3)
    private BigDecimal fueledLiters;

    @Column(name = "divergence_liters", nullable = false, precision = 12, scale = 3)
    private BigDecimal divergenceLiters;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ShiftReconciliation() {}

    public ShiftReconciliation(UUID id, Shift shift, Nozzle nozzle, BigDecimal openingTotalizer,
                               BigDecimal closingTotalizer, BigDecimal measuredLiters,
                               BigDecimal fueledLiters, BigDecimal divergenceLiters,
                               LocalDateTime createdAt) {
        this.id = id;
        this.shift = shift;
        this.nozzle = nozzle;
        this.openingTotalizer = openingTotalizer;
        this.closingTotalizer = closingTotalizer;
        this.measuredLiters = measuredLiters;
        this.fueledLiters = fueledLiters;
        this.divergenceLiters = divergenceLiters;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Shift getShift() { return shift; }
    public Nozzle getNozzle() { return nozzle; }
    public BigDecimal getOpeningTotalizer() { return openingTotalizer; }
    public BigDecimal getClosingTotalizer() { return closingTotalizer; }
    public BigDecimal getMeasuredLiters() { return measuredLiters; }
    public BigDecimal getFueledLiters() { return fueledLiters; }
    public BigDecimal getDivergenceLiters() { return divergenceLiters; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
