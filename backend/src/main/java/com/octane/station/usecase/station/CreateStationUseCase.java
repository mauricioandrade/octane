package com.octane.station.usecase.station;

import com.octane.shared.exception.BusinessException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateStationUseCase {

    private final StationRepository stationRepository;

    public CreateStationUseCase(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Transactional
    public Station execute(CreateStationRequest request) {
        if (stationRepository.findByCnpj(request.cnpj()).isPresent()) {
            throw new BusinessException("CNPJ já cadastrado: " + request.cnpj());
        }
        var now = LocalDateTime.now();
        var station = new Station(null, request.name(), request.cnpj(),
            request.address(), request.city(), request.state(), true, now, now);
        return stationRepository.save(station);
    }
}
