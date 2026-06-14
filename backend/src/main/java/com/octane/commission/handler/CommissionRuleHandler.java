package com.octane.commission.handler;

import com.octane.commission.usecase.CommissionRuleResponse;
import com.octane.commission.usecase.rule.CreateCommissionRuleRequest;
import com.octane.commission.usecase.rule.CreateCommissionRuleUseCase;
import com.octane.commission.usecase.rule.ListCommissionRulesUseCase;
import com.octane.commission.usecase.rule.UpdateCommissionRuleRequest;
import com.octane.commission.usecase.rule.UpdateCommissionRuleUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/commission/rules")
public class CommissionRuleHandler {

    private final CreateCommissionRuleUseCase createCommissionRuleUseCase;
    private final ListCommissionRulesUseCase listCommissionRulesUseCase;
    private final UpdateCommissionRuleUseCase updateCommissionRuleUseCase;

    public CommissionRuleHandler(CreateCommissionRuleUseCase createCommissionRuleUseCase,
                                  ListCommissionRulesUseCase listCommissionRulesUseCase,
                                  UpdateCommissionRuleUseCase updateCommissionRuleUseCase) {
        this.createCommissionRuleUseCase = createCommissionRuleUseCase;
        this.listCommissionRulesUseCase = listCommissionRulesUseCase;
        this.updateCommissionRuleUseCase = updateCommissionRuleUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommissionRuleResponse create(@Valid @RequestBody CreateCommissionRuleRequest request) {
        return createCommissionRuleUseCase.execute(request);
    }

    @GetMapping
    public List<CommissionRuleResponse> list(
            @RequestParam UUID stationId,
            @RequestParam(required = false) Boolean active) {
        return listCommissionRulesUseCase.execute(stationId, active);
    }

    @PutMapping("/{id}")
    public CommissionRuleResponse update(@PathVariable UUID id,
                                          @Valid @RequestBody UpdateCommissionRuleRequest request) {
        return updateCommissionRuleUseCase.execute(id, request);
    }

    @PatchMapping("/{id}/status")
    public CommissionRuleResponse updateStatus(@PathVariable UUID id,
                                                @RequestBody Map<String, Boolean> body) {
        var active = body.get("active");
        var request = new UpdateCommissionRuleRequest(null, null, active);
        return updateCommissionRuleUseCase.execute(id, request);
    }
}
