package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterFuelingUseCase {

    private final ShiftRepository shiftRepository;
    private final FuelingRepository fuelingRepository;
    private final NozzleRepository nozzleRepository;

    public RegisterFuelingUseCase(ShiftRepository shiftRepository,
                                  FuelingRepository fuelingRepository,
                                  NozzleRepository nozzleRepository) {
        this.shiftRepository = shiftRepository;
        this.fuelingRepository = fuelingRepository;
        this.nozzleRepository = nozzleRepository;
    }

    @Transactional
    public Fueling execute(UUID shiftId, RegisterFuelingRequest request) {
        var shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new EntityNotFoundException("Shift not found: " + shiftId));

        if (shift.getStatus() != ShiftStatus.OPEN) {
            throw new BusinessException("Turno não está aberto");
        }

        var nozzleId = request.nozzleId();
        var nozzle = nozzleRepository.findById(nozzleId)
                .orElseThrow(() -> new EntityNotFoundException("Nozzle not found: " + nozzleId));

        if (!nozzle.getPump().getStation().getId().equals(shift.getStation().getId())) {
            throw new BusinessException("Bico não pertence ao posto deste turno");
        }

        PaymentMethod paymentMethod = PaymentMethod.valueOf(request.paymentMethod());

        BigDecimal calculated = request.liters().multiply(request.unitPrice());
        if (calculated.subtract(request.totalAmount()).abs().compareTo(new BigDecimal("0.01")) > 0) {
            throw new BusinessException("Valor total não confere com liters × preço unitário");
        }

        var now = LocalDateTime.now();
        var fueling = new Fueling(
                null, shift, nozzle,
                request.liters(), request.unitPrice(), request.totalAmount(),
                paymentMethod, request.vehiclePlate(), request.notes(),
                now, now
        );
        return fuelingRepository.save(fueling);
    }
}
