package com.octane.fueling.usecase.fueling;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.pricing.domain.repository.FuelPriceRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RegisterFuelingUseCase {

    private static final BigDecimal TOLERANCE = new BigDecimal("0.01");

    private final ShiftRepository shiftRepository;
    private final FuelingRepository fuelingRepository;
    private final NozzleRepository nozzleRepository;
    private final FuelPriceRepository fuelPriceRepository;

    public RegisterFuelingUseCase(ShiftRepository shiftRepository,
                                  FuelingRepository fuelingRepository,
                                  NozzleRepository nozzleRepository,
                                  FuelPriceRepository fuelPriceRepository) {
        this.shiftRepository = shiftRepository;
        this.fuelingRepository = fuelingRepository;
        this.nozzleRepository = nozzleRepository;
        this.fuelPriceRepository = fuelPriceRepository;
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

        if (!shift.getStation().isActive()) {
            throw new BusinessException("Posto inativo");
        }
        if (nozzle.getPump().getStatus() != PumpStatus.ACTIVE) {
            throw new BusinessException("Bomba não está ativa");
        }
        if (!nozzle.isActive()) {
            throw new BusinessException("Bico inativo");
        }

        var stationId = shift.getStation().getId();
        var fuelId = nozzle.getFuel().getId();
        var price = fuelPriceRepository.findCurrent(stationId, fuelId)
                .orElseThrow(() -> new BusinessException(
                    "Sem preço vigente para o combustível " + nozzle.getFuel().getName()))
                .getPrice();

        BigDecimal liters = request.liters();
        BigDecimal totalAmount = request.totalAmount();

        if (liters == null && totalAmount == null) {
            throw new BusinessException("Informe litros ou valor total");
        }
        if (liters == null) {
            liters = totalAmount.divide(price, 3, RoundingMode.HALF_UP);
        } else if (totalAmount == null) {
            totalAmount = liters.multiply(price).setScale(2, RoundingMode.HALF_UP);
        } else {
            BigDecimal calculated = liters.multiply(price).setScale(2, RoundingMode.HALF_UP);
            if (calculated.subtract(totalAmount).abs().compareTo(TOLERANCE) > 0) {
                throw new BusinessException("Valor total não confere com litros × preço vigente");
            }
        }

        PaymentMethod paymentMethod = PaymentMethod.valueOf(request.paymentMethod());

        var now = LocalDateTime.now();
        var fueling = new Fueling(
                null, shift, nozzle,
                liters, price, totalAmount,
                paymentMethod, FuelingStatus.ACTIVE, null,
                request.vehiclePlate(), request.notes(),
                now, now
        );
        return fuelingRepository.save(fueling);
    }
}
