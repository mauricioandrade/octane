package com.octane.inventory.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tank_movements")
public class TankMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "tank_id", nullable = false)
    private Tank tank;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TankMovementType type;

    @Column(name = "volume_liters", nullable = false, precision = 12, scale = 3)
    private BigDecimal volumeLiters;

    @Column(name = "previous_level", nullable = false, precision = 12, scale = 2)
    private BigDecimal previousLevel;

    @Column(name = "new_level", nullable = false, precision = 12, scale = 2)
    private BigDecimal newLevel;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public TankMovement() {}

    public UUID getId() { return id; }
    public Tank getTank() { return tank; }
    public TankMovementType getType() { return type; }
    public BigDecimal getVolumeLiters() { return volumeLiters; }
    public BigDecimal getPreviousLevel() { return previousLevel; }
    public BigDecimal getNewLevel() { return newLevel; }
    public UUID getReferenceId() { return referenceId; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setTank(Tank tank) { this.tank = tank; }
    public void setType(TankMovementType type) { this.type = type; }
    public void setVolumeLiters(BigDecimal volumeLiters) { this.volumeLiters = volumeLiters; }
    public void setPreviousLevel(BigDecimal previousLevel) { this.previousLevel = previousLevel; }
    public void setNewLevel(BigDecimal newLevel) { this.newLevel = newLevel; }
    public void setReferenceId(UUID referenceId) { this.referenceId = referenceId; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
