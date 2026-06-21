package com.octane.commission.usecase;

import com.octane.commission.domain.CommissionRule;
import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.commission.usecase.rule.CreateCommissionRuleRequest;
import com.octane.commission.usecase.rule.CreateCommissionRuleUseCase;
import com.octane.shared.exception.BusinessException;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateCommissionRuleUseCaseTest {

    @Mock
    private CommissionRuleRepository commissionRuleRepository;

    @Mock
    private StationRepository stationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CreateCommissionRuleUseCase sut;

    private final Station station = new Station(UUID.randomUUID(), "Posto X", "12.345.678/0001-90",
            "Rua A, 1", "São Paulo", "SP", true, LocalDateTime.now(), LocalDateTime.now());

    @Test
    void execute_createsRule_withValidRequest() {
        var request = new CreateCommissionRuleRequest(station.getId(), "João Silva", new BigDecimal("0.0200"));

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(commissionRuleRepository.findByEmployeeNameAndStationId("João Silva", station.getId()))
                .thenReturn(Optional.empty());
        when(commissionRuleRepository.save(any(CommissionRule.class))).thenAnswer(inv -> inv.getArgument(0));

        var result = sut.execute(request);

        assertThat(result.employeeName()).isEqualTo("João Silva");
        assertThat(result.rate()).isEqualByComparingTo("0.0200");
        assertThat(result.active()).isTrue();
        verify(commissionRuleRepository).save(any(CommissionRule.class));
    }

    @Test
    void execute_throwsBusinessException_whenRuleAlreadyExists() {
        var request = new CreateCommissionRuleRequest(station.getId(), "João Silva", new BigDecimal("0.0200"));
        var existingRule = new CommissionRule(UUID.randomUUID(), station, "João Silva",
                new BigDecimal("0.0150"), true, LocalDateTime.now());

        when(stationRepository.findById(station.getId())).thenReturn(Optional.of(station));
        when(commissionRuleRepository.findByEmployeeNameAndStationId("João Silva", station.getId()))
                .thenReturn(Optional.of(existingRule));

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Já existe regra ativa para este funcionário");

        verify(commissionRuleRepository, never()).save(any());
    }
}
