package com.octane.fueling.handler;

import com.octane.fueling.usecase.fueling.CancelFuelingUseCase;
import com.octane.fueling.usecase.fueling.FuelingResponse;
import com.octane.fueling.usecase.fueling.ListFuelingsByShiftUseCase;
import com.octane.fueling.usecase.fueling.RegisterFuelingRequest;
import com.octane.fueling.usecase.fueling.RegisterFuelingUseCase;
import com.octane.fueling.usecase.fueling.ShiftSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/shifts/{shiftId}/fuelings")
public class FuelingHandler {

    private final RegisterFuelingUseCase registerFuelingUseCase;
    private final ListFuelingsByShiftUseCase listFuelingsByShiftUseCase;
    private final CancelFuelingUseCase cancelFuelingUseCase;

    public FuelingHandler(
        RegisterFuelingUseCase registerFuelingUseCase,
        ListFuelingsByShiftUseCase listFuelingsByShiftUseCase,
        CancelFuelingUseCase cancelFuelingUseCase
    ) {
        this.registerFuelingUseCase = registerFuelingUseCase;
        this.listFuelingsByShiftUseCase = listFuelingsByShiftUseCase;
        this.cancelFuelingUseCase = cancelFuelingUseCase;
    }

    @PostMapping("/{fuelingId}/cancel")
    public FuelingResponse cancelFueling(@PathVariable UUID shiftId, @PathVariable UUID fuelingId) {
        return FuelingResponse.from(cancelFuelingUseCase.execute(shiftId, fuelingId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuelingResponse registerFueling(@PathVariable UUID shiftId, @Valid @RequestBody RegisterFuelingRequest request) {
        return FuelingResponse.from(registerFuelingUseCase.execute(shiftId, request));
    }

    @GetMapping
    public ShiftSummaryResponse listFuelings(@PathVariable UUID shiftId) {
        return listFuelingsByShiftUseCase.execute(shiftId);
    }
}
