package com.octane.station.usecase.pump;

import com.octane.audit.usecase.AuditService;
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
    private final AuditService auditService;

    public CreatePumpUseCase(StationRepository stationRepository, PumpRepository pumpRepository,
                             AuditService auditService) {
        this.stationRepository = stationRepository;
        this.pumpRepository = pumpRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Pump execute(UUID stationId, CreatePumpRequest request) {
        var station = stationRepository.findById(stationId)
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + stationId));

        if (pumpRepository.existsByStationIdAndNumber(stationId, request.number())) {
            throw new BusinessException("Bomba número " + request.number() + " já existe neste posto");
        }

        var now = LocalDateTime.now();
        var pump = new Pump(null, request.number(), PumpStatus.ACTIVE, station, now, now);
        var saved = pumpRepository.save(pump);
        auditService.log("CREATE", "Pump", saved.getId(), "bomba " + saved.getNumber());
        return saved;
    }
}
