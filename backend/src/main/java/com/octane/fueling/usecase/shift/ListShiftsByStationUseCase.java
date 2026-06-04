package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListShiftsByStationUseCase {

    private final ShiftRepository shiftRepository;
    private final StationRepository stationRepository;

    public ListShiftsByStationUseCase(ShiftRepository shiftRepository, StationRepository stationRepository) {
        this.shiftRepository = shiftRepository;
        this.stationRepository = stationRepository;
    }

    public List<Shift> execute(UUID stationId) {
        stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));
        return shiftRepository.findByStationId(stationId);
    }
}
