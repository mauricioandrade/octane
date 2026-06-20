package com.octane.financial.handler;

import com.octane.financial.usecase.*;
import com.octane.shared.pagination.PageResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/cash-registers")
public class CashRegisterHandler {

    private final OpenCashRegisterUseCase openCashRegisterUseCase;
    private final CloseCashRegisterUseCase closeCashRegisterUseCase;
    private final AddCashMovementUseCase addCashMovementUseCase;
    private final GetCashRegisterSummaryUseCase getCashRegisterSummaryUseCase;
    private final ListCashRegistersUseCase listCashRegistersUseCase;

    public CashRegisterHandler(OpenCashRegisterUseCase openCashRegisterUseCase,
                                CloseCashRegisterUseCase closeCashRegisterUseCase,
                                AddCashMovementUseCase addCashMovementUseCase,
                                GetCashRegisterSummaryUseCase getCashRegisterSummaryUseCase,
                                ListCashRegistersUseCase listCashRegistersUseCase) {
        this.openCashRegisterUseCase = openCashRegisterUseCase;
        this.closeCashRegisterUseCase = closeCashRegisterUseCase;
        this.addCashMovementUseCase = addCashMovementUseCase;
        this.getCashRegisterSummaryUseCase = getCashRegisterSummaryUseCase;
        this.listCashRegistersUseCase = listCashRegistersUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CashRegisterResponse open(@Valid @RequestBody OpenCashRegisterUseCase.Request request) {
        return CashRegisterResponse.from(openCashRegisterUseCase.execute(request));
    }

    @PostMapping("/{id}/close")
    public CashRegisterResponse close(@PathVariable UUID id,
                                       @Valid @RequestBody CloseCashRegisterUseCase.Request request) {
        return closeCashRegisterUseCase.execute(id, request);
    }

    @PostMapping("/{id}/movements")
    @ResponseStatus(HttpStatus.CREATED)
    public CashMovementResponse addMovement(@PathVariable UUID id,
                                             @Valid @RequestBody AddCashMovementUseCase.Request request) {
        return addCashMovementUseCase.execute(id, request);
    }

    @GetMapping("/{id}")
    public GetCashRegisterSummaryUseCase.SummaryResponse find(@PathVariable UUID id) {
        return getCashRegisterSummaryUseCase.execute(id);
    }

    @GetMapping
    public PageResponse<CashRegisterResponse> list(
            @RequestParam UUID stationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return listCashRegistersUseCase.execute(stationId, page, size);
    }
}
