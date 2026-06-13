package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.shared.pagination.PageResponse;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ListShiftsByStationUseCase {

    private final ShiftRepository shiftRepository;
    private final StationRepository stationRepository;

    public ListShiftsByStationUseCase(ShiftRepository shiftRepository, StationRepository stationRepository) {
        this.shiftRepository = shiftRepository;
        this.stationRepository = stationRepository;
    }

    public PageResponse<Shift> execute(UUID stationId, String status,
                                       LocalDateTime from, LocalDateTime to,
                                       int page, int size) {
        stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));
        ShiftStatus shiftStatus = status != null ? ShiftStatus.valueOf(status) : null;
        return shiftRepository.findByStationId(stationId, shiftStatus, from, to, page, size);
    }
}
