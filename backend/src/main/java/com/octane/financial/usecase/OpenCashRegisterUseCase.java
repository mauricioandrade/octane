package com.octane.financial.usecase;

import com.octane.financial.domain.CashRegister;
import com.octane.financial.domain.CashRegisterStatus;
import com.octane.financial.domain.repository.CashRegisterRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.station.domain.repository.StationRepository;
import com.octane.shared.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OpenCashRegisterUseCase {

    private final CashRegisterRepository cashRegisterRepository;
    private final StationRepository stationRepository;

    public OpenCashRegisterUseCase(CashRegisterRepository cashRegisterRepository,
                                    StationRepository stationRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.stationRepository = stationRepository;
    }

    public record Request(@NotNull UUID stationId, @NotNull BigDecimal openingBalance, String notes) {}

    @Transactional
    public CashRegister execute(@Valid Request request) {
        var station = stationRepository.findById(request.stationId())
            .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado"));

        cashRegisterRepository.findOpenByStationId(request.stationId())
            .ifPresent(r -> { throw new BusinessException("Já existe um caixa aberto para este posto"); });

        var register = new CashRegister();
        register.setStation(station);
        register.setOpenedAt(LocalDateTime.now());
        register.setOpeningBalance(request.openingBalance());
        register.setStatus(CashRegisterStatus.OPEN);
        register.setNotes(request.notes());
        register.setCreatedAt(LocalDateTime.now());
        return cashRegisterRepository.save(register);
    }
}
