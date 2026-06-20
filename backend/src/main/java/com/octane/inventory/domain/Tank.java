package com.octane.inventory.domain;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.Station;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tanks")
public class Tank {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @ManyToOne
    @JoinColumn(name = "fuel_id", nullable = false)
    private Fuel fuel;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal capacity;

    @Column(name = "current_level", nullable = false, precision = 12, scale = 2)
    private BigDecimal currentLevel;

    @Column(name = "minimum_level", nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumLevel;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Tank() {}

    public UUID getId() { return id; }
    public Station getStation() { return station; }
    public Fuel getFuel() { return fuel; }
    public String getName() { return name; }
    public BigDecimal getCapacity() { return capacity; }
    public BigDecimal getCurrentLevel() { return currentLevel; }
    public BigDecimal getMinimumLevel() { return minimumLevel; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStation(Station station) { this.station = station; }
    public void setFuel(Fuel fuel) { this.fuel = fuel; }
    public void setName(String name) { this.name = name; }
    public void setCapacity(BigDecimal capacity) { this.capacity = capacity; }
    public void setCurrentLevel(BigDecimal currentLevel) { this.currentLevel = currentLevel; }
    public void setMinimumLevel(BigDecimal minimumLevel) { this.minimumLevel = minimumLevel; }
    public void setActive(boolean active) { this.active = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
