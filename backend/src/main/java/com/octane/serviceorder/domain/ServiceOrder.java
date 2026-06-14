package com.octane.serviceorder.domain;

import com.octane.station.domain.Station;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "service_orders")
public class ServiceOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "station_id", nullable = false)
    private Station station;

    @Column(nullable = false, length = 10)
    private String plate;

    @Column(nullable = false)
    private Integer odometer;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ServiceOrderStatus status;

    @Column(length = 500)
    private String notes;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ServiceOrder() {}

    public ServiceOrder(UUID id, Station station, String plate, Integer odometer,
                        String customerName, String customerPhone, ServiceOrderStatus status,
                        String notes, LocalDateTime openedAt, LocalDateTime closedAt,
                        LocalDateTime createdAt) {
        this.id = id;
        this.station = station;
        this.plate = plate;
        this.odometer = odometer;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.status = status;
        this.notes = notes;
        this.openedAt = openedAt;
        this.closedAt = closedAt;
        this.cancelledAt = null;
        this.createdAt = createdAt;
    }

    public UUID getId() { return id; }
    public Station getStation() { return station; }
    public String getPlate() { return plate; }
    public Integer getOdometer() { return odometer; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public ServiceOrderStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public LocalDateTime getOpenedAt() { return openedAt; }
    public LocalDateTime getClosedAt() { return closedAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setStatus(ServiceOrderStatus status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setClosedAt(LocalDateTime closedAt) { this.closedAt = closedAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
}
