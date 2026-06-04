package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.NozzleReadingType;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.NozzleReadingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.NozzleRepository;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CloseShiftUseCase {

    private final ShiftRepository shiftRepository;
    private final NozzleReadingRepository nozzleReadingRepository;
    private final NozzleRepository nozzleRepository;
    private final PumpRepository pumpRepository;

    public CloseShiftUseCase(ShiftRepository shiftRepository,
                             NozzleReadingRepository nozzleReadingRepository,
                             NozzleRepository nozzleRepository,
                             PumpRepository pumpRepository) {
        this.shiftRepository = shiftRepository;
        this.nozzleReadingRepository = nozzleReadingRepository;
        this.nozzleRepository = nozzleRepository;
        this.pumpRepository = pumpRepository;
    }

    @Transactional
    public Shift execute(UUID shiftId) {
        var shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + shiftId));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessException("Turno já está fechado");
        }

        List<UUID> activeNozzleIds = pumpRepository.findByStationId(shift.getStation().getId())
                .stream()
                .flatMap(pump -> nozzleRepository.findByPumpId(pump.getId()).stream())
                .filter(nozzle -> nozzle.isActive())
                .map(nozzle -> nozzle.getId())
                .toList();

        var closingReadings = nozzleReadingRepository.findByShiftId(shiftId);
        List<UUID> nozzlesWithClosing = closingReadings.stream()
                .filter(r -> r.getType() == NozzleReadingType.CLOSING)
                .map(r -> r.getNozzle().getId())
                .toList();

        boolean allNozzlesClosed = activeNozzleIds.stream().allMatch(nozzlesWithClosing::contains);
        if (!allNozzlesClosed) {
            throw new BusinessException("Faltam leituras de fechamento para todos os bicos ativos");
        }

        var closedShift = new Shift(
                shift.getId(),
                shift.getStation(),
                shift.getEmployeeName(),
                ShiftStatus.CLOSED,
                shift.getOpenedAt(),
                LocalDateTime.now(),
                shift.getNotes(),
                shift.getCreatedAt()
        );
        return shiftRepository.save(closedShift);
    }
}
