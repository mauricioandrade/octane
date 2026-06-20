package com.octane.financial.usecase;

import com.octane.financial.domain.CashMovement;
import com.octane.financial.domain.CashRegisterStatus;
import com.octane.financial.domain.MovementCategory;
import com.octane.financial.domain.MovementType;
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
public class AddCashMovementUseCase {

    private final CashRegisterRepository cashRegisterRepository;

    public AddCashMovementUseCase(CashRegisterRepository cashRegisterRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
    }

    public record Request(
        @NotNull String type, @NotNull String category,
        String description, @NotNull BigDecimal amount, String paymentMethod
    ) {}

    @Transactional
    public CashMovementResponse execute(UUID registerId, @Valid Request request) {
        var register = cashRegisterRepository.findById(registerId)
            .orElseThrow(() -> new EntityNotFoundException("Caixa não encontrado"));

        if (register.getStatus() != CashRegisterStatus.OPEN) {
            throw new BusinessException("Não é possível adicionar movimentação em caixa fechado");
        }

        var movement = new CashMovement();
        movement.setCashRegister(register);
        movement.setType(MovementType.valueOf(request.type()));
        movement.setCategory(MovementCategory.valueOf(request.category()));
        movement.setDescription(request.description());
        movement.setAmount(request.amount());
        movement.setPaymentMethod(request.paymentMethod());
        movement.setCreatedAt(LocalDateTime.now());
        return CashMovementResponse.from(cashRegisterRepository.saveMovement(movement));
    }
}
