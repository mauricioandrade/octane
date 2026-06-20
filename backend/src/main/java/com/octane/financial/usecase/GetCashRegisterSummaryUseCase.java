package com.octane.financial.usecase;

import com.octane.financial.domain.MovementType;
import com.octane.financial.domain.repository.CashRegisterRepository;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class GetCashRegisterSummaryUseCase {

    private final CashRegisterRepository cashRegisterRepository;

    public GetCashRegisterSummaryUseCase(CashRegisterRepository cashRegisterRepository) {
        this.cashRegisterRepository = cashRegisterRepository;
    }

    public record SummaryResponse(
        CashRegisterResponse register,
        List<CashMovementResponse> movements,
        BigDecimal totalIncome,
        BigDecimal totalExpense,
        BigDecimal balance
    ) {}

    @Transactional(readOnly = true)
    public SummaryResponse execute(UUID id) {
        var register = cashRegisterRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Caixa não encontrado"));

        var movements = cashRegisterRepository.findMovementsByRegisterId(id);

        var totalIncome = movements.stream()
            .filter(m -> m.getType() == MovementType.INCOME)
            .map(m -> m.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        var totalExpense = movements.stream()
            .filter(m -> m.getType() == MovementType.EXPENSE)
            .map(m -> m.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        var balance = register.getOpeningBalance().add(totalIncome).subtract(totalExpense);

        return new SummaryResponse(
            CashRegisterResponse.from(register),
            movements.stream().map(CashMovementResponse::from).toList(),
            totalIncome, totalExpense, balance
        );
    }
}
