package com.octane.fueling.domain;

import com.octane.station.domain.Nozzle;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nozzle_readings")
public class NozzleReading {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "shift_id", nullable = false)
    private Shift shift;

    @ManyToOne
    @JoinColumn(name = "nozzle_id", nullable = false)
    private Nozzle nozzle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private NozzleReadingType type;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal totalizer;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    public NozzleReading() {}

    public NozzleReading(UUID id, Shift shift, Nozzle nozzle, NozzleReadingType type,
                         BigDecimal totalizer, LocalDateTime recordedAt) {
        this.id = id;
        this.shift = shift;
        this.nozzle = nozzle;
        this.type = type;
        this.totalizer = totalizer;
        this.recordedAt = recordedAt;
    }

    public UUID getId() { return id; }
    public Shift getShift() { return shift; }
    public Nozzle getNozzle() { return nozzle; }
    public NozzleReadingType getType() { return type; }
    public BigDecimal getTotalizer() { return totalizer; }
    public LocalDateTime getRecordedAt() { return recordedAt; }
}
