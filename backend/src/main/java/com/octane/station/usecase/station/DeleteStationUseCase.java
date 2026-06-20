package com.octane.station.usecase.station;

import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DeleteStationUseCase {

    private final StationRepository stationRepository;

    public DeleteStationUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    public void execute(UUID id) {
        var station = stationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado"));
        station.setDeletedAt(LocalDateTime.now());
        stationRepository.save(station);
    }
}
