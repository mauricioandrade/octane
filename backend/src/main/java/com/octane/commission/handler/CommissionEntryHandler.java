package com.octane.commission.handler;

import com.octane.commission.usecase.CommissionEntryResponse;
import com.octane.commission.usecase.entry.CalculateCommissionUseCase;
import com.octane.commission.usecase.entry.GetShiftCommissionUseCase;
import com.octane.commission.usecase.entry.ListCommissionEntriesUseCase;
import com.octane.commission.usecase.entry.MarkCommissionPaidUseCase;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
public class CommissionEntryHandler {

    private final CalculateCommissionUseCase calculateCommissionUseCase;
    private final ListCommissionEntriesUseCase listCommissionEntriesUseCase;
    private final GetShiftCommissionUseCase getShiftCommissionUseCase;
    private final MarkCommissionPaidUseCase markCommissionPaidUseCase;

    public CommissionEntryHandler(CalculateCommissionUseCase calculateCommissionUseCase,
                                   ListCommissionEntriesUseCase listCommissionEntriesUseCase,
                                   GetShiftCommissionUseCase getShiftCommissionUseCase,
                                   MarkCommissionPaidUseCase markCommissionPaidUseCase) {
        this.calculateCommissionUseCase = calculateCommissionUseCase;
        this.listCommissionEntriesUseCase = listCommissionEntriesUseCase;
        this.getShiftCommissionUseCase = getShiftCommissionUseCase;
        this.markCommissionPaidUseCase = markCommissionPaidUseCase;
    }

    @PostMapping("/api/commission/calculate/{shiftId}")
    public ResponseEntity<CommissionEntryResponse> calculate(@PathVariable UUID shiftId) {
        var result = calculateCommissionUseCase.execute(shiftId);
        return result
                .map(entry -> ResponseEntity.ok(CommissionEntryResponse.from(entry)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/api/commission/entries")
    public List<CommissionEntryResponse> list(
            @RequestParam UUID stationId,
            @RequestParam(required = false) Boolean paid,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return listCommissionEntriesUseCase.execute(stationId, paid, from, to);
    }

    @GetMapping("/api/commission/shifts/{shiftId}/entry")
    public ResponseEntity<CommissionEntryResponse> getShiftEntry(@PathVariable UUID shiftId) {
        return getShiftCommissionUseCase.execute(shiftId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/api/commission/entries/{id}/pay")
    public CommissionEntryResponse pay(@PathVariable UUID id) {
        return markCommissionPaidUseCase.execute(id);
    }
}
