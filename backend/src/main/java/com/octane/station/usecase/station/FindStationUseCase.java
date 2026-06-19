package com.octane.station.usecase.station;

import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FindStationUseCase {

    private final StationRepository stationRepository;

    public FindStationUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public Station execute(UUID id) {
        return stationRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + id));
    }
}
