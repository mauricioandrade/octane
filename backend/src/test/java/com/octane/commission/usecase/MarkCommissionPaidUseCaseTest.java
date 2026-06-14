package com.octane.commission.usecase;

import com.octane.commission.domain.CommissionEntry;
import com.octane.commission.domain.repository.CommissionEntryRepository;
import com.octane.commission.usecase.entry.MarkCommissionPaidUseCase;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.shared.exception.BusinessException;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MarkCommissionPaidUseCaseTest {

    @Mock
    private CommissionEntryRepository commissionEntryRepository;

    @InjectMocks
    private MarkCommissionPaidUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

    private CommissionEntry buildUnpaidEntry() {
        var shift = new Shift(UUID.randomUUID(), station, "João Silva", ShiftStatus.CLOSED,
                LocalDateTime.now().minusHours(8), LocalDateTime.now(), null, LocalDateTime.now());
        return new CommissionEntry(UUID.randomUUID(), shift, "João Silva", station,
                new BigDecimal("1000.00"), new BigDecimal("0.0200"), new BigDecimal("20.00"),
                false, null, LocalDateTime.now());
    }

    @Test
    void execute_marksAsPaid_whenNotPaid() {
        var entry = buildUnpaidEntry();

        when(commissionEntryRepository.findById(entry.getId())).thenReturn(Optional.of(entry));
        when(commissionEntryRepository.save(any(CommissionEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(entry.getId());

        assertThat(result.paid()).isTrue();
        assertThat(result.paidAt()).isNotNull();
    }

    @Test
    void execute_throwsBusinessException_whenAlreadyPaid() {
        var shift = new Shift(UUID.randomUUID(), station, "João Silva", ShiftStatus.CLOSED,
                LocalDateTime.now().minusHours(8), LocalDateTime.now(), null, LocalDateTime.now());
        var paidEntry = new CommissionEntry(UUID.randomUUID(), shift, "João Silva", station,
                new BigDecimal("1000.00"), new BigDecimal("0.0200"), new BigDecimal("20.00"),
                true, LocalDateTime.now(), LocalDateTime.now());

        when(commissionEntryRepository.findById(paidEntry.getId())).thenReturn(Optional.of(paidEntry));

        assertThatThrownBy(() -> sut.execute(paidEntry.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Comissão já marcada como paga");
    }
}
