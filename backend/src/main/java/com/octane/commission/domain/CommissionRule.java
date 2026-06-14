package com.octane.commission.domain;

import com.octane.station.domain.Station;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "commission_rules")
public class CommissionRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal rate;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public CommissionRule() {}

    public CommissionRule(UUID id, Station station, String employeeName, BigDecimal rate,
                          boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.station = station;
        this.employeeName = employeeName;
        this.rate = rate;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Station getStation() { return station; }
    public String getEmployeeName() { return employeeName; }
    public BigDecimal getRate() { return rate; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setRate(BigDecimal rate) { this.rate = rate; }
    public void setActive(boolean active) { this.active = active; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
}
