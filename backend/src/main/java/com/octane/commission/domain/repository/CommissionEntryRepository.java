package com.octane.commission.domain.repository;

import com.octane.commission.domain.CommissionEntry;
import com.octane.shared.pagination.PageResponse;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface CommissionEntryRepository {
    CommissionEntry save(CommissionEntry entry);
    Optional<CommissionEntry> findById(UUID id);
    Optional<CommissionEntry> findByShiftId(UUID shiftId);
    PageResponse<CommissionEntry> findByStationId(UUID stationId, Boolean paid, LocalDate from, LocalDate to,
                                                   int page, int size);
}
