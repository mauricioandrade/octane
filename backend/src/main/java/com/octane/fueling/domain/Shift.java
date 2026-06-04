package com.octane.fueling.domain;

import com.octane.station.domain.Station;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "shifts")
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(name = "employee_name", nullable = false, length = 100)
    private String employeeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShiftStatus status;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public Shift() {}

    public Shift(UUID id, Station station, String employeeName, ShiftStatus status,
                 LocalDateTime openedAt, LocalDateTime closedAt, String notes, LocalDateTime createdAt) {
        this.id = id;
        this.station = station;
        this.employeeName = employeeName;
        this.status = status;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Station getStation() { return station; }
    public String getEmployeeName() { return employeeName; }
    public ShiftStatus getStatus() { return status; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
