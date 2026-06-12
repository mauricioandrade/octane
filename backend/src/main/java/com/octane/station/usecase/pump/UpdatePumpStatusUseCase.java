package com.octane.station.usecase.pump;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdatePumpStatusUseCase {

    private final PumpRepository pumpRepository;

    public UpdatePumpStatusUseCase(PumpRepository pumpRepository) {
        this.pumpRepository = pumpRepository;
    }

    @Transactional
    public Pump execute(UUID id, UpdatePumpStatusRequest request) {
        var pump = pumpRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Pump not found: " + id));

        PumpStatus status = PumpStatus.valueOf(request.status());

        var updated = new Pump(pump.getId(), pump.getNumber(), status,
            pump.getStation(), pump.getCreatedAt(), LocalDateTime.now());
        return pumpRepository.save(updated);
    }
}
