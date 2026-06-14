package com.octane.commission.usecase.rule;

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

    public CreateCommissionRuleUseCase(CommissionRuleRepository commissionRuleRepository,
                                       StationRepository stationRepository) {
        this.commissionRuleRepository = commissionRuleRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public CommissionRuleResponse execute(CreateCommissionRuleRequest request) {
        var stationId = request.stationId();
        var station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Station not found: " + stationId));

        commissionRuleRepository.findByEmployeeNameAndStationId(request.employeeName(), stationId)
                .filter(CommissionRule::isActive)
                .ifPresent(existing -> {
                    throw new BusinessException("Já existe regra ativa para este funcionário");
                });

        var now = LocalDateTime.now();
        var rule = new CommissionRule(null, station, request.employeeName(), request.rate(), true, now);
        var saved = commissionRuleRepository.save(rule);
        return CommissionRuleResponse.from(saved);
    }
}
