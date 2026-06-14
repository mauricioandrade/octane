package com.octane.commission.usecase;

import com.octane.commission.domain.CommissionEntry;
import com.octane.commission.domain.CommissionRule;
import com.octane.commission.domain.repository.CommissionEntryRepository;
import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.commission.usecase.entry.CalculateCommissionUseCase;
import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.FuelingStatus;
import com.octane.fueling.domain.PaymentMethod;
import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.FuelingRepository;
import com.octane.fueling.domain.repository.ShiftRepository;
import com.octane.shared.exception.BusinessException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculateCommissionUseCaseTest {

    @Mock
    private ShiftRepository shiftRepository;

    @Mock
    private FuelingRepository fuelingRepository;

    @Mock
    private CommissionRuleRepository commissionRuleRepository;

    @Mock
    private CommissionEntryRepository commissionEntryRepository;

    @InjectMocks
    private CalculateCommissionUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

    private Shift buildClosedShift() {
        return new Shift(UUID.randomUUID(), station, "João Silva", ShiftStatus.CLOSED,
                LocalDateTime.now().minusHours(8), LocalDateTime.now(), null, LocalDateTime.now());
    }

    @Test
    void execute_calculatesCommission_whenRuleExists() {
        var shift = buildClosedShift();
        var rule = new CommissionRule(UUID.randomUUID(), station, "João Silva",
                new BigDecimal("0.0200"), true, LocalDateTime.now());

        var fueling = new Fueling(UUID.randomUUID(), shift, null, new BigDecimal("50.000"),
                new BigDecimal("6.00"), new BigDecimal("1000.00"), PaymentMethod.CASH,
                FuelingStatus.ACTIVE, null, null, null, LocalDateTime.now(), LocalDateTime.now());

        when(shiftRepository.findById(shift.getId())).thenReturn(Optional.of(shift));
        when(commissionEntryRepository.findByShiftId(shift.getId())).thenReturn(Optional.empty());
        when(commissionRuleRepository.findByEmployeeNameAndStationId("João Silva", station.getId()))
                .thenReturn(Optional.of(rule));
        when(fuelingRepository.findByShiftId(shift.getId())).thenReturn(List.of(fueling));
        when(commissionEntryRepository.save(any(CommissionEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(shift.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getBaseAmount()).isEqualByComparingTo("1000.00");
        assertThat(result.get().getCommission()).isEqualByComparingTo("20.00");
        assertThat(result.get().getRate()).isEqualByComparingTo("0.0200");
    }

    @Test
    void execute_returnsEmpty_whenNoRuleForEmployee() {
        var shift = buildClosedShift();

        when(shiftRepository.findById(shift.getId())).thenReturn(Optional.of(shift));
        when(commissionEntryRepository.findByShiftId(shift.getId())).thenReturn(Optional.empty());
        when(commissionRuleRepository.findByEmployeeNameAndStationId("João Silva", station.getId()))
                .thenReturn(Optional.empty());

        var result = sut.execute(shift.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void execute_throwsBusinessException_whenShiftNotClosed() {
        var openShift = new Shift(UUID.randomUUID(), station, "João Silva", ShiftStatus.OPEN,
                LocalDateTime.now(), null, null, LocalDateTime.now());

        when(shiftRepository.findById(openShift.getId())).thenReturn(Optional.of(openShift));

        assertThatThrownBy(() -> sut.execute(openShift.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Turno não está fechado");
    }

    @Test
    void execute_throwsBusinessException_whenCommissionAlreadyCalculated() {
        var shift = buildClosedShift();
        var existingEntry = new CommissionEntry(UUID.randomUUID(), shift, "João Silva", station,
                new BigDecimal("1000.00"), new BigDecimal("0.0200"), new BigDecimal("20.00"),
                false, null, LocalDateTime.now());

        when(shiftRepository.findById(shift.getId())).thenReturn(Optional.of(shift));
        when(commissionEntryRepository.findByShiftId(shift.getId())).thenReturn(Optional.of(existingEntry));

        assertThatThrownBy(() -> sut.execute(shift.getId()))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Comissão já calculada para este turno");
    }
}
