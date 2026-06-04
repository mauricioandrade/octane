package com.octane.station.usecase.pump;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.repository.PumpRepository;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreatePumpUseCase {

    private final StationRepository stationRepository;
    private final PumpRepository pumpRepository;

    public CreatePumpUseCase(StationRepository stationRepository, PumpRepository pumpRepository) {
        this.stationRepository = stationRepository;
        this.pumpRepository = pumpRepository;
    }

    @Transactional
    public Pump execute(UUID stationId, CreatePumpRequest request) {
        var station = stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));

        if (pumpRepository.existsByStationIdAndNumber(stationId, request.number())) {
            throw new BusinessException("Bomba número " + request.number() + " já existe neste posto");
        }

        var now = LocalDateTime.now();
        var pump = new Pump(null, request.number(), PumpStatus.ACTIVE, station, now, now);
        return pumpRepository.save(pump);
    }
}
