package com.octane.station.usecase.pump;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdatePumpUseCase {

    private final PumpRepository pumpRepository;

    public UpdatePumpUseCase(PumpRepository pumpRepository) {
        this.pumpRepository = pumpRepository;
    }

    @Transactional
    public Pump execute(UUID id, UpdatePumpRequest request) {
        var pump = pumpRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pump not found: " + id));

        if (request.number() != pump.getNumber()
                && pumpRepository.existsByStationIdAndNumber(pump.getStation().getId(), request.number())) {
            throw new BusinessException("Bomba número " + request.number() + " já existe neste posto");
        }

        var updated = new Pump(pump.getId(), request.number(), pump.getStatus(),
            pump.getStation(), pump.getCreatedAt(), LocalDateTime.now());
        return pumpRepository.save(updated);
    }
}
