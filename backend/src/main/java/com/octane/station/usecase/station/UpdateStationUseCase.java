package com.octane.station.usecase.station;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateStationUseCase {

    private final StationRepository stationRepository;
    private final AuditService auditService;

    public UpdateStationUseCase(StationRepository stationRepository, AuditService auditService) {
        this.stationRepository = stationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Station execute(UUID id, UpdateStationRequest request) {
        var station = stationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + id));

        var conflicting = stationRepository.findByCnpj(request.cnpj());
        if (conflicting.isPresent() && !conflicting.get().getId().equals(id)) {
            throw new BusinessException("CNPJ já cadastrado: " + request.cnpj());
        }

        var updated = new Station(station.getId(), request.name(), request.cnpj(),
            request.address(), request.city(), request.state(), station.isActive(),
            station.getCreatedAt(), LocalDateTime.now());
        var saved = stationRepository.save(updated);
        auditService.log("UPDATE", "Station", saved.getId(), saved.getName());
        return saved;
    }
}
