package com.octane.commission.usecase;

import com.octane.commission.domain.CommissionRule;
import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.commission.usecase.rule.DeleteCommissionRuleUseCase;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Station;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeleteCommissionRuleUseCaseTest {

    @Mock
    private CommissionRuleRepository commissionRuleRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private DeleteCommissionRuleUseCase sut;

    @Test
    void execute_setsDeletedAt() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var station = new Station(UUID.randomUUID(), "P", "00.000.000/0001-00", "A", "C", "SP", true, now, now);
        var rule = new CommissionRule(id, station, "João", new BigDecimal("0.0200"), true, now);

        when(commissionRuleRepository.findById(id)).thenReturn(Optional.of(rule));
        when(commissionRuleRepository.save(rule)).thenReturn(rule);

        sut.execute(id);

        assertThat(rule.getDeletedAt()).isNotNull();
        verify(commissionRuleRepository).save(rule);
    }

    @Test
    void execute_throws_whenNotFound() {
        var id = UUID.randomUUID();
        when(commissionRuleRepository.findById(id)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> sut.execute(id)).isInstanceOf(EntityNotFoundException.class);
    }
}
