package com.octane.financial.usecase;

import com.octane.financial.domain.CashMovement;
import com.octane.financial.domain.CashRegister;
import com.octane.financial.domain.CashRegisterStatus;
import com.octane.financial.domain.MovementCategory;
import com.octane.financial.domain.MovementType;
import com.octane.financial.domain.repository.CashRegisterRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddCashMovementUseCaseTest {

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @InjectMocks
    private AddCashMovementUseCase sut;

    private CashRegister openRegister() {
        var register = new CashRegister();
        register.setStation(new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP",
                true, LocalDateTime.now(), LocalDateTime.now()));
        register.setOpenedAt(LocalDateTime.now());
        register.setOpeningBalance(new BigDecimal("500.00"));
        register.setStatus(CashRegisterStatus.OPEN);
        register.setCreatedAt(LocalDateTime.now());
        return register;
    }

    @Test
    void execute_addsMovement_whenRegisterOpen() {
        var registerId = UUID.randomUUID();
        var register = openRegister();
        var request = new AddCashMovementUseCase.Request("INCOME", "FUEL_SALE", "Venda", new BigDecimal("100.00"), "PIX");

        var savedMovement = new CashMovement();
        savedMovement.setCashRegister(register);
        savedMovement.setType(MovementType.INCOME);
        savedMovement.setCategory(MovementCategory.FUEL_SALE);
        savedMovement.setDescription("Venda");
        savedMovement.setAmount(new BigDecimal("100.00"));
        savedMovement.setPaymentMethod("PIX");
        savedMovement.setCreatedAt(LocalDateTime.now());

        when(cashRegisterRepository.findById(registerId)).thenReturn(Optional.of(register));
        when(cashRegisterRepository.saveMovement(any(CashMovement.class))).thenReturn(savedMovement);

        var result = sut.execute(registerId, request);

        assertThat(result.type()).isEqualTo("INCOME");
        assertThat(result.category()).isEqualTo("FUEL_SALE");
        assertThat(result.amount()).isEqualByComparingTo("100.00");
        verify(cashRegisterRepository).saveMovement(any(CashMovement.class));
    }

    @Test
    void execute_throws_whenRegisterClosed() {
        var registerId = UUID.randomUUID();
        var register = openRegister();
        register.setStatus(CashRegisterStatus.CLOSED);
        var request = new AddCashMovementUseCase.Request("INCOME", "OTHER", null, BigDecimal.TEN, null);

        when(cashRegisterRepository.findById(registerId)).thenReturn(Optional.of(register));

        assertThatThrownBy(() -> sut.execute(registerId, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("caixa fechado");

        verify(cashRegisterRepository, never()).saveMovement(any());
    }

    @Test
    void execute_throws_whenNotFound() {
        var registerId = UUID.randomUUID();
        var request = new AddCashMovementUseCase.Request("INCOME", "OTHER", null, BigDecimal.TEN, null);

        when(cashRegisterRepository.findById(registerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(registerId, request))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
