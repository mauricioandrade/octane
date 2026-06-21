package com.octane.station.usecase.station;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateStationStatusUseCase {

    private final StationRepository stationRepository;
    private final AuditService auditService;

    public UpdateStationStatusUseCase(StationRepository stationRepository, AuditService auditService) {
        this.stationRepository = stationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Station execute(UUID id, UpdateStationStatusRequest request) {
        var station = stationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + id));

        var updated = new Station(station.getId(), station.getName(), station.getCnpj(),
            station.getAddress(), station.getCity(), station.getState(), request.active(),
            station.getCreatedAt(), LocalDateTime.now());
        var saved = stationRepository.save(updated);
        auditService.log("UPDATE", "Station", saved.getId(), "status=" + request.active());
        return saved;
    }
}
