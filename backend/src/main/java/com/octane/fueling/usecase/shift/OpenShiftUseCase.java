package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class OpenShiftUseCase {

    private final ShiftRepository shiftRepository;
    private final StationRepository stationRepository;

    public OpenShiftUseCase(ShiftRepository shiftRepository, StationRepository stationRepository) {
        this.shiftRepository = shiftRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public Shift execute(OpenShiftRequest request) {
        var stationId = request.stationId();
        var station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));

        if (!station.isActive()) {
            throw new BusinessException("Posto inativo: não é possível abrir turno");
        }

        if (shiftRepository.findOpenByStationId(stationId).isPresent()) {
            throw new BusinessException("Já existe um turno aberto para este posto");
        }

        var now = LocalDateTime.now();
        var shift = new Shift(null, station, request.employeeName(), ShiftStatus.OPEN, now, null, request.notes(), now);
        return shiftRepository.save(shift);
    }
}
