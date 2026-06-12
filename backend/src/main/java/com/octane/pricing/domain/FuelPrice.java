package com.octane.pricing.domain;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.Station;
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
@Table(name = "fuel_prices")
public class FuelPrice {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne
    @JoinColumn(name = "fuel_id", nullable = false)
    private Fuel fuel;

    @Column(nullable = false, precision = 10, scale = 4)
    private BigDecimal price;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FuelPrice() {}

    public FuelPrice(UUID id, Station station, Fuel fuel, BigDecimal price,
                     LocalDateTime effectiveFrom, LocalDateTime createdAt) {
        this.id = id;
        this.station = station;
        this.fuel = fuel;
        this.price = price;
        this.effectiveFrom = effectiveFrom;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Station getStation() { return station; }
    public Fuel getFuel() { return fuel; }
    public BigDecimal getPrice() { return price; }
    public LocalDateTime getEffectiveFrom() { return effectiveFrom; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
