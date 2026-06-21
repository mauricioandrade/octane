package com.octane.financial.usecase;

import com.octane.financial.domain.CashRegister;
import com.octane.financial.domain.CashRegisterStatus;
import com.octane.financial.domain.repository.CashRegisterRepository;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import com.octane.audit.usecase.AuditService;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenCashRegisterUseCaseTest {

    @Mock
    private CashRegisterRepository cashRegisterRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private OpenCashRegisterUseCase sut;

    @Test
    void execute_opensCashRegister_whenNoneOpen() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A", "SP", "SP",
                true, LocalDateTime.now(), LocalDateTime.now());
        var request = new OpenCashRegisterUseCase.Request(stationId, new BigDecimal("500.00"), null);

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(cashRegisterRepository.findOpenByStationId(stationId)).thenReturn(Optional.empty());
        when(cashRegisterRepository.save(any(CashRegister.class))).thenAnswer(inv -> inv.getArgument(0));

        sut.execute(request);

        verify(cashRegisterRepository).save(any(CashRegister.class));
    }

    @Test
    void execute_throws_whenStationNotFound() {
        var stationId = UUID.randomUUID();
        var request = new OpenCashRegisterUseCase.Request(stationId, BigDecimal.TEN, null);

        when(stationRepository.findById(stationId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(EntityNotFoundException.class);

        verify(cashRegisterRepository, never()).save(any());
    }

    @Test
    void execute_throws_whenCashRegisterAlreadyOpen() {
        var stationId = UUID.randomUUID();
        var station = new Station(stationId, "Posto X", "12.345.678/0001-90", "Rua A", "SP", "SP",
                true, LocalDateTime.now(), LocalDateTime.now());
        var request = new OpenCashRegisterUseCase.Request(stationId, BigDecimal.TEN, null);
        var existing = new CashRegister();

        when(stationRepository.findById(stationId)).thenReturn(Optional.of(station));
        when(cashRegisterRepository.findOpenByStationId(stationId)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe");

        verify(cashRegisterRepository, never()).save(any());
    }
}
