package com.octane.station.usecase.station;

import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListStationsUseCase {

    private final StationRepository stationRepository;

    public ListStationsUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    public List<Station> execute() {
        return stationRepository.findAll();
    }
}
