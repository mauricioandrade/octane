package com.octane.financial.usecase;

import com.octane.audit.usecase.AuditService;
import com.octane.financial.domain.CashRegisterStatus;
import com.octane.financial.domain.repository.CashRegisterRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CloseCashRegisterUseCase {

    private final CashRegisterRepository cashRegisterRepository;
    private final AuditService auditService;

    public CloseCashRegisterUseCase(CashRegisterRepository cashRegisterRepository,
                                    AuditService auditService) {
        this.cashRegisterRepository = cashRegisterRepository;
        this.auditService = auditService;
    }

    public record Request(@NotNull BigDecimal closingBalance) {}

    @Transactional
    public CashRegisterResponse execute(UUID id, @Valid Request request) {
        var register = cashRegisterRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Caixa não encontrado"));

        if (register.getStatus() != CashRegisterStatus.OPEN) {
            throw new BusinessException("Caixa já está fechado");
        }

        register.setClosingBalance(request.closingBalance());
        register.setClosedAt(LocalDateTime.now());
        register.setStatus(CashRegisterStatus.CLOSED);
        var saved = cashRegisterRepository.save(register);
        auditService.log("CLOSE", "CashRegister", saved.getId(), null);
        return CashRegisterResponse.from(saved);
    }
}
