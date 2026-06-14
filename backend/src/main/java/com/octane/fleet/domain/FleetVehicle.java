package com.octane.fleet.domain;

import com.octane.station.domain.Fuel;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fleet_vehicles")
public class FleetVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private FleetClient client;

    @Column(nullable = false, length = 10)
    private String plate;

    @Column(length = 100)
    private String model;

    @ManyToOne
    @JoinColumn(name = "allowed_fuel_id", nullable = false)
    private Fuel allowedFuel;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FleetVehicle() {}

    public FleetVehicle(UUID id, FleetClient client, String plate, String model,
                        Fuel allowedFuel, boolean active, LocalDateTime createdAt) {
        this.id = id;
        this.client = client;
        this.plate = plate;
        this.model = model;
        this.allowedFuel = allowedFuel;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public FleetClient getClient() { return client; }
    public String getPlate() { return plate; }
    public String getModel() { return model; }
    public Fuel getAllowedFuel() { return allowedFuel; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setModel(String model) { this.model = model; }
    public void setAllowedFuel(Fuel allowedFuel) { this.allowedFuel = allowedFuel; }
    public void setActive(boolean active) { this.active = active; }
}
