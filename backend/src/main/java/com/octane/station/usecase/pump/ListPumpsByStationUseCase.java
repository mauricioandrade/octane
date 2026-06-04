package com.octane.station.usecase.pump;

import com.octane.station.domain.Pump;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListPumpsByStationUseCase {

    private final PumpRepository pumpRepository;

    public ListPumpsByStationUseCase(PumpRepository pumpRepository) {
        this.pumpRepository = pumpRepository;
    }

    public List<Pump> execute(UUID stationId) {
        return pumpRepository.findByStationId(stationId);
    }
}
