package com.octane.fueling.usecase.shift;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.NozzleReading;
import com.octane.fueling.domain.NozzleReadingType;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftReconciliation;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.NozzleReadingRepository;
import com.octane.fueling.domain.repository.ShiftReconciliationRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.NozzleRepository;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CloseShiftUseCase {

    private final ShiftRepository shiftRepository;
    private final NozzleReadingRepository nozzleReadingRepository;
    private final NozzleRepository nozzleRepository;
    private final PumpRepository pumpRepository;
    private final FuelingRepository fuelingRepository;
    private final ShiftReconciliationRepository shiftReconciliationRepository;

    public CloseShiftUseCase(ShiftRepository shiftRepository,
                             NozzleReadingRepository nozzleReadingRepository,
                             NozzleRepository nozzleRepository,
                             PumpRepository pumpRepository,
                             FuelingRepository fuelingRepository,
                             ShiftReconciliationRepository shiftReconciliationRepository) {
        this.shiftRepository = shiftRepository;
        this.nozzleReadingRepository = nozzleReadingRepository;
        this.nozzleRepository = nozzleRepository;
        this.pumpRepository = pumpRepository;
        this.fuelingRepository = fuelingRepository;
        this.shiftReconciliationRepository = shiftReconciliationRepository;
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

        var readings = nozzleReadingRepository.findByShiftId(shiftId);
        Map<UUID, NozzleReading> openingByNozzle = readings.stream()
                .filter(r -> r.getType() == NozzleReadingType.OPENING)
                .collect(Collectors.toMap(r -> r.getNozzle().getId(), r -> r));
        Map<UUID, NozzleReading> closingByNozzle = readings.stream()
                .filter(r -> r.getType() == NozzleReadingType.CLOSING)
                .collect(Collectors.toMap(r -> r.getNozzle().getId(), r -> r));

        boolean allNozzlesClosed = activeNozzleIds.stream().allMatch(closingByNozzle::containsKey);
        if (!allNozzlesClosed) {
            throw new BusinessException("Faltam leituras de fechamento para todos os bicos ativos");
        }

        Map<UUID, BigDecimal> fueledByNozzle = fuelingRepository.findByShiftId(shiftId).stream()
                .filter(f -> f.getStatus() == FuelingStatus.ACTIVE)
                .collect(Collectors.groupingBy(f -> f.getNozzle().getId(),
                        Collectors.reducing(BigDecimal.ZERO, Fueling::getLiters, BigDecimal::add)));

        var now = LocalDateTime.now();
        List<ShiftReconciliation> reconciliations = closingByNozzle.entrySet().stream()
                .map(entry -> {
                    var opening = openingByNozzle.get(entry.getKey());
                    var closing = entry.getValue();
                    var openingTotalizer = opening != null ? opening.getTotalizer() : BigDecimal.ZERO;
                    var measured = closing.getTotalizer().subtract(openingTotalizer);
                    var fueled = fueledByNozzle.getOrDefault(entry.getKey(), BigDecimal.ZERO);
                    return new ShiftReconciliation(null, shift, closing.getNozzle(),
                            openingTotalizer, closing.getTotalizer(),
                            measured, fueled, measured.subtract(fueled), now);
                })
                .toList();
        shiftReconciliationRepository.saveAll(reconciliations);

        var closedShift = new Shift(
                shift.getId(),
                shift.getStation(),
                shift.getEmployeeName(),
                ShiftStatus.CLOSED,
                shift.getOpenedAt(),
                now,
                shift.getNotes(),
                shift.getCreatedAt()
        );
        return shiftRepository.save(closedShift);
    }
}
