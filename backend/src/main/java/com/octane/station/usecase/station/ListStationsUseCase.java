package com.octane.station.usecase.station;

import com.octane.shared.auth.AuthenticatedUserService;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListStationsUseCase {

    private final StationRepository stationRepository;
    private final AuthenticatedUserService authenticatedUserService;

    public ListStationsUseCase(StationRepository stationRepository,
                               AuthenticatedUserService authenticatedUserService) {
        this.stationRepository = stationRepository;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<Station> execute(Boolean active) {
        var currentUser = authenticatedUserService.getCurrentUser();
        var stations = stationRepository.findAll(active);

        if (currentUser.isAdmin()) {
            return stations;
        }

        var allowedIds = currentUser.stationIds();
        return stations.stream()
                .filter(s -> allowedIds.contains(s.getId()))
                .toList();
    }
}
