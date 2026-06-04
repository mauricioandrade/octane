package com.octane.fueling.usecase.reading;

import com.octane.fueling.domain.NozzleReading;
import com.octane.fueling.domain.NozzleReadingType;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.NozzleReadingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterNozzleReadingUseCase {

    private final ShiftRepository shiftRepository;
    private final NozzleReadingRepository nozzleReadingRepository;
    private final NozzleRepository nozzleRepository;

    public RegisterNozzleReadingUseCase(ShiftRepository shiftRepository,
                                        NozzleReadingRepository nozzleReadingRepository,
                                        NozzleRepository nozzleRepository) {
        this.shiftRepository = shiftRepository;
        this.nozzleReadingRepository = nozzleReadingRepository;
        this.nozzleRepository = nozzleRepository;
    }

    @Transactional
    public NozzleReading execute(UUID shiftId, RegisterNozzleReadingRequest request) {
        var shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + shiftId));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessException("Turno não está aberto");
        }

        NozzleReadingType type = NozzleReadingType.valueOf(request.type());

        var nozzleId = request.nozzleId();
        var nozzle = nozzleRepository.findById(nozzleId)
                .orElseThrow(() -> new EntityNotFoundException("Nozzle not found: " + nozzleId));

        if (nozzleReadingRepository.findByShiftIdAndNozzleIdAndType(shiftId, nozzleId, type).isPresent()) {
            throw new BusinessException("Leitura do tipo " + type + " já registrada para este bico neste turno");
        }

        if (type == NozzleReadingType.CLOSING) {
            var openingReading = nozzleReadingRepository
                    .findByShiftIdAndNozzleIdAndType(shiftId, nozzleId, NozzleReadingType.OPENING);
            if (openingReading.isPresent()) {
                if (request.totalizer().compareTo(openingReading.get().getTotalizer()) < 0) {
                    throw new BusinessException("Leitura de fechamento não pode ser menor que a leitura de abertura");
                }
            }
        }

        var reading = new NozzleReading(null, shift, nozzle, type, request.totalizer(), LocalDateTime.now());
        return nozzleReadingRepository.save(reading);
    }
}
