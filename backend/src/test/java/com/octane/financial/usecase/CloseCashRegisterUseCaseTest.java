package com.octane.financial.usecase;

import com.octane.financial.domain.CashRegister;
import com.octane.financial.domain.CashRegisterStatus;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CloseCashRegisterUseCaseTest {

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @InjectMocks
    private CloseCashRegisterUseCase sut;

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
    void execute_closesRegister() {
        var id = UUID.randomUUID();
        var register = openRegister();
        var request = new CloseCashRegisterUseCase.Request(new BigDecimal("1500.00"));

        when(cashRegisterRepository.findById(id)).thenReturn(Optional.of(register));
        when(cashRegisterRepository.save(any(CashRegister.class))).thenReturn(register);

        var result = sut.execute(id, request);

        assertThat(register.getStatus()).isEqualTo(CashRegisterStatus.CLOSED);
        assertThat(register.getClosingBalance()).isEqualByComparingTo("1500.00");
        verify(cashRegisterRepository).save(register);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();
        var request = new CloseCashRegisterUseCase.Request(BigDecimal.TEN);

        when(cashRegisterRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_throws_whenAlreadyClosed() {
        var id = UUID.randomUUID();
        var register = openRegister();
        register.setStatus(CashRegisterStatus.CLOSED);
        var request = new CloseCashRegisterUseCase.Request(BigDecimal.TEN);

        when(cashRegisterRepository.findById(id)).thenReturn(Optional.of(register));

        assertThatThrownBy(() -> sut.execute(id, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já está fechado");
    }
}
