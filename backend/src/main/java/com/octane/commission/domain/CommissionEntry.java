package com.octane.commission.domain;

import com.octane.fueling.domain.Shift;
import com.octane.station.domain.Station;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "commission_entries")
public class CommissionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "base_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseAmount;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal commission;

    @Column(nullable = false)
    private boolean paid;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CommissionEntry() {}

    public CommissionEntry(UUID id, Shift shift, String employeeName, Station station,
                           BigDecimal baseAmount, BigDecimal rate, BigDecimal commission,
                           boolean paid, LocalDateTime paidAt, LocalDateTime createdAt) {
        this.id = id;
        this.shift = shift;
        this.employeeName = employeeName;
        this.station = station;
        this.baseAmount = baseAmount;
        this.rate = rate;
        this.commission = commission;
        this.paid = paid;
        this.paidAt = paidAt;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Shift getShift() { return shift; }
    public String getEmployeeName() { return employeeName; }
    public Station getStation() { return station; }
    public BigDecimal getBaseAmount() { return baseAmount; }
    public BigDecimal getRate() { return rate; }
    public BigDecimal getCommission() { return commission; }
    public boolean isPaid() { return paid; }
    public LocalDateTime getPaidAt() { return paidAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setPaid(boolean paid) { this.paid = paid; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }
}
