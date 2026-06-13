package com.octane.fueling.domain.repository;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.shared.pagination.PageResponse;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface ShiftRepository {
    Shift save(Shift shift);
    Optional<Shift> findById(UUID id);
    Optional<Shift> findOpenByStationId(UUID stationId);
    PageResponse<Shift> findByStationId(UUID stationId, ShiftStatus status,
                                        LocalDateTime from, LocalDateTime to,
                                        int page, int size);
}
