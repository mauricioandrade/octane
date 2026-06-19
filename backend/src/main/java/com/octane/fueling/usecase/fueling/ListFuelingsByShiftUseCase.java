package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ListFuelingsByShiftUseCase {

    private final ShiftRepository shiftRepository;
    private final FuelingRepository fuelingRepository;

    public ListFuelingsByShiftUseCase(ShiftRepository shiftRepository,
                                      FuelingRepository fuelingRepository) {
        this.shiftRepository = shiftRepository;
        this.fuelingRepository = fuelingRepository;
    }

    public ShiftSummaryResponse execute(UUID shiftId) {
        shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Turno não encontrado: " + shiftId));

        List<Fueling> fuelings = fuelingRepository.findByShiftId(shiftId).stream()
                .filter(f -> f.getStatus() == FuelingStatus.ACTIVE)
                .toList();

        BigDecimal totalLiters = fuelings.stream()
                .map(Fueling::getLiters)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAmount = fuelings.stream()
                .map(Fueling::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ShiftSummaryResponse(
                shiftId,
                fuelings.stream().map(FuelingResponse::from).toList(),
                totalLiters,
                totalAmount
        );
    }
}
