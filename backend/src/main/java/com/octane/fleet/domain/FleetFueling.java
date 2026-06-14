package com.octane.fleet.domain;

import com.octane.fueling.domain.Fueling;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fleet_fuelings")
public class FleetFueling {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "fueling_id", nullable = false)
    private Fueling fueling;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private FleetDriver driver;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private FleetVehicle vehicle;

    @Column(nullable = false)
    private Integer odometer;

    @Column(name = "previous_odometer")
    private Integer previousOdometer;

    @Column(name = "odometer_alert", nullable = false)
    private boolean odometerAlert;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public FleetFueling() {}

    public FleetFueling(UUID id, Fueling fueling, FleetDriver driver, FleetVehicle vehicle,
                        Integer odometer, Integer previousOdometer, boolean odometerAlert,
                        LocalDateTime createdAt) {
        this.id = id;
        this.fueling = fueling;
        this.driver = driver;
        this.vehicle = vehicle;
        this.odometer = odometer;
        this.previousOdometer = previousOdometer;
        this.odometerAlert = odometerAlert;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Fueling getFueling() { return fueling; }
    public FleetDriver getDriver() { return driver; }
    public FleetVehicle getVehicle() { return vehicle; }
    public Integer getOdometer() { return odometer; }
    public Integer getPreviousOdometer() { return previousOdometer; }
    public boolean isOdometerAlert() { return odometerAlert; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
