package com.octane.fleet.domain;

import com.octane.station.domain.Station;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fleet_clients")
public class FleetClient {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false, length = 18)
    private String cnpj;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "trade_name", length = 100)
    private String tradeName;

    @Column(name = "monthly_limit", precision = 12, scale = 2)
    private BigDecimal monthlyLimit;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FleetClient() {}

    public FleetClient(UUID id, Station station, String cnpj, String companyName,
                       String tradeName, BigDecimal monthlyLimit, boolean active,
                       LocalDateTime createdAt) {
        this.id = id;
        this.station = station;
        this.cnpj = cnpj;
        this.companyName = companyName;
        this.tradeName = tradeName;
        this.monthlyLimit = monthlyLimit;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Station getStation() { return station; }
    public String getCnpj() { return cnpj; }
    public String getCompanyName() { return companyName; }
    public String getTradeName() { return tradeName; }
    public BigDecimal getMonthlyLimit() { return monthlyLimit; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public void setMonthlyLimit(BigDecimal monthlyLimit) { this.monthlyLimit = monthlyLimit; }
    public void setActive(boolean active) { this.active = active; }
}
