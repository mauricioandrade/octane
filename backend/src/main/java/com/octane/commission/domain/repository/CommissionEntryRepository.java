package com.octane.commission.domain.repository;

import com.octane.commission.domain.CommissionEntry;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommissionEntryRepository {
    CommissionEntry save(CommissionEntry entry);
    Optional<CommissionEntry> findById(UUID id);
    Optional<CommissionEntry> findByShiftId(UUID shiftId);
    List<CommissionEntry> findByStationId(UUID stationId, Boolean paid, LocalDate from, LocalDate to);
}
