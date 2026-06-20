package com.octane.financial.usecase;

import com.octane.financial.domain.CashMovement;
import com.octane.financial.domain.CashRegister;
import com.octane.financial.domain.CashRegisterStatus;
import com.octane.financial.domain.MovementCategory;
import com.octane.financial.domain.MovementType;
import com.octane.financial.domain.repository.CashRegisterRepository;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCashRegisterSummaryUseCaseTest {

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @InjectMocks
    private GetCashRegisterSummaryUseCase sut;

    @Test
    void execute_returnsSummary_withCorrectTotals() {
        var registerId = UUID.randomUUID();
        var register = new CashRegister();
        register.setStation(new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP",
                true, LocalDateTime.now(), LocalDateTime.now()));
        register.setOpenedAt(LocalDateTime.now());
        register.setOpeningBalance(new BigDecimal("500.00"));
        register.setStatus(CashRegisterStatus.OPEN);
        register.setCreatedAt(LocalDateTime.now());

        var income = new CashMovement();
        income.setCashRegister(register);
        income.setType(MovementType.INCOME);
        income.setCategory(MovementCategory.FUEL_SALE);
        income.setAmount(new BigDecimal("300.00"));
        income.setCreatedAt(LocalDateTime.now());

        var expense = new CashMovement();
        expense.setCashRegister(register);
        expense.setType(MovementType.EXPENSE);
        expense.setCategory(MovementCategory.SUPPLY_PURCHASE);
        expense.setAmount(new BigDecimal("100.00"));
        expense.setCreatedAt(LocalDateTime.now());

        when(cashRegisterRepository.findById(registerId)).thenReturn(Optional.of(register));
        when(cashRegisterRepository.findMovementsByRegisterId(registerId)).thenReturn(List.of(income, expense));

        var result = sut.execute(registerId);

        assertThat(result.totalIncome()).isEqualByComparingTo("300.00");
        assertThat(result.totalExpense()).isEqualByComparingTo("100.00");
        assertThat(result.balance()).isEqualByComparingTo("700.00"); // 500 + 300 - 100
        assertThat(result.movements()).hasSize(2);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();

        when(cashRegisterRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
