package com.octane.commission.handler;

import com.octane.commission.usecase.CommissionEntryResponse;
import com.octane.commission.usecase.entry.CalculateCommissionUseCase;
import com.octane.commission.usecase.entry.GetShiftCommissionUseCase;
import com.octane.commission.usecase.entry.ListCommissionEntriesUseCase;
import com.octane.commission.usecase.entry.MarkCommissionPaidUseCase;
import com.octane.shared.auth.AuthenticatedUserService;
import com.octane.shared.pagination.PageResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/commission")
public class CommissionEntryHandler {

    private final CalculateCommissionUseCase calculateCommissionUseCase;
    private final ListCommissionEntriesUseCase listCommissionEntriesUseCase;
    private final GetShiftCommissionUseCase getShiftCommissionUseCase;
    private final MarkCommissionPaidUseCase markCommissionPaidUseCase;
    private final AuthenticatedUserService authService;

    public CommissionEntryHandler(CalculateCommissionUseCase calculateCommissionUseCase,
                                   ListCommissionEntriesUseCase listCommissionEntriesUseCase,
                                   GetShiftCommissionUseCase getShiftCommissionUseCase,
                                   MarkCommissionPaidUseCase markCommissionPaidUseCase,
                                   AuthenticatedUserService authService) {
        this.calculateCommissionUseCase = calculateCommissionUseCase;
        this.listCommissionEntriesUseCase = listCommissionEntriesUseCase;
        this.getShiftCommissionUseCase = getShiftCommissionUseCase;
        this.markCommissionPaidUseCase = markCommissionPaidUseCase;
        this.authService = authService;
    }

    @PostMapping("/calculate/{shiftId}")
    public ResponseEntity<CommissionEntryResponse> calculate(@PathVariable UUID shiftId) {
        var result = calculateCommissionUseCase.execute(shiftId);
        return result
                .map(entry -> ResponseEntity.ok(CommissionEntryResponse.from(entry)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/entries")
    public PageResponse<CommissionEntryResponse> list(
            @RequestParam UUID stationId,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        authService.validateStationAccess(stationId);
        return listCommissionEntriesUseCase.execute(stationId, paid, from, to, page, size);
    }

    @GetMapping("/shifts/{shiftId}/entry")
    public ResponseEntity<CommissionEntryResponse> getShiftEntry(@PathVariable UUID shiftId) {
        return getShiftCommissionUseCase.execute(shiftId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/entries/{id}/pay")
    public CommissionEntryResponse pay(@PathVariable UUID id) {
        return markCommissionPaidUseCase.execute(id);
    }
}
