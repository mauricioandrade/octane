package com.octane.pricing.handler;

import com.octane.pricing.usecase.FuelPriceResponse;
import com.octane.pricing.usecase.GetCurrentPricesUseCase;
import com.octane.pricing.usecase.ListPriceHistoryUseCase;
import com.octane.pricing.usecase.SetFuelPriceRequest;
import com.octane.pricing.usecase.SetFuelPriceUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/stations/{stationId}/prices")
public class FuelPriceHandler {

    private final SetFuelPriceUseCase setFuelPriceUseCase;
    private final GetCurrentPricesUseCase getCurrentPricesUseCase;
    private final ListPriceHistoryUseCase listPriceHistoryUseCase;

    public FuelPriceHandler(
        SetFuelPriceUseCase setFuelPriceUseCase,
        GetCurrentPricesUseCase getCurrentPricesUseCase,
        ListPriceHistoryUseCase listPriceHistoryUseCase
    ) {
        this.setFuelPriceUseCase = setFuelPriceUseCase;
        this.getCurrentPricesUseCase = getCurrentPricesUseCase;
        this.listPriceHistoryUseCase = listPriceHistoryUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuelPriceResponse setPrice(@PathVariable UUID stationId, @Valid @RequestBody SetFuelPriceRequest request) {
        return FuelPriceResponse.from(setFuelPriceUseCase.execute(stationId, request));
    }

    @GetMapping
    public List<FuelPriceResponse> currentPrices(@PathVariable UUID stationId) {
        return getCurrentPricesUseCase.execute(stationId).stream()
            .map(FuelPriceResponse::from)
            .toList();
    }

    @GetMapping("/history")
    public List<FuelPriceResponse> history(@PathVariable UUID stationId, @RequestParam UUID fuelId) {
        return listPriceHistoryUseCase.execute(stationId, fuelId).stream()
            .map(FuelPriceResponse::from)
            .toList();
    }
}
