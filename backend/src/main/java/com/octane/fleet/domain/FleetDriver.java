package com.octane.fleet.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "fleet_drivers")
@SQLRestriction("deleted_at IS NULL")
public class FleetDriver {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private FleetClient client;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 14)
    private String cpf;

    @Column(name = "pin_hash", length = 60)
    private String pinHash;

    @Column(name = "rfid_tag", length = 50)
    private String rfidTag;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public FleetDriver() {}

    public FleetDriver(UUID id, FleetClient client, String name, String cpf,
                       String pinHash, String rfidTag, boolean active,
                       LocalDateTime createdAt) {
        this.id = id;
        this.client = client;
        this.name = name;
        this.cpf = cpf;
        this.pinHash = pinHash;
        this.rfidTag = rfidTag;
        this.active = active;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public FleetClient getClient() { return client; }
    public String getName() { return name; }
    public String getCpf() { return cpf; }
    public String getPinHash() { return pinHash; }
    public String getRfidTag() { return rfidTag; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setName(String name) { this.name = name; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }
    public void setRfidTag(String rfidTag) { this.rfidTag = rfidTag; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }
}
