package com.octane.commission.usecase.rule;

import com.octane.audit.usecase.AuditService;
import com.octane.commission.domain.CommissionRule;
import com.octane.commission.domain.repository.CommissionRuleRepository;
import com.octane.commission.usecase.CommissionRuleResponse;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateCommissionRuleUseCase {

    private final CommissionRuleRepository commissionRuleRepository;
    private final StationRepository stationRepository;
    private final AuditService auditService;

    public CreateCommissionRuleUseCase(CommissionRuleRepository commissionRuleRepository,
                                       StationRepository stationRepository,
                                       AuditService auditService) {
        this.commissionRuleRepository = commissionRuleRepository;
        this.stationRepository = stationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public CommissionRuleResponse execute(CreateCommissionRuleRequest request) {
        var stationId = request.stationId();
        var station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + stationId));

        var existing = commissionRuleRepository.findByEmployeeNameAndStationId(request.employeeName(), stationId);

        if (existing.isPresent()) {
            var rule = existing.get();
            if (rule.isActive()) {
                throw new BusinessException("Já existe regra ativa para este funcionário");
            }
            rule.setRate(request.rate());
            rule.setActive(true);
            var saved = commissionRuleRepository.save(rule);
            return CommissionRuleResponse.from(saved);
        }

        var now = LocalDateTime.now();
        var rule = new CommissionRule(null, station, request.employeeName(), request.rate(), true, now);
        var saved = commissionRuleRepository.save(rule);
        auditService.log("CREATE", "CommissionRule", saved.getId(), saved.getEmployeeName());
        return CommissionRuleResponse.from(saved);
    }
}
