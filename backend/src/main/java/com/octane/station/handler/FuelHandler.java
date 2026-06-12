package com.octane.station.handler;

import com.octane.station.usecase.fuel.ListFuelsUseCase;
import com.octane.station.usecase.fuel.UpdateFuelStatusRequest;
import com.octane.station.usecase.fuel.UpdateFuelStatusUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fuels")
public class FuelHandler {

    private final ListFuelsUseCase listFuelsUseCase;
    private final UpdateFuelStatusUseCase updateFuelStatusUseCase;

    public FuelHandler(ListFuelsUseCase listFuelsUseCase,
                       UpdateFuelStatusUseCase updateFuelStatusUseCase) {
        this.listFuelsUseCase = listFuelsUseCase;
        this.updateFuelStatusUseCase = updateFuelStatusUseCase;
    }

    @PatchMapping("/{id}/status")
    public FuelResponse updateStatus(@PathVariable UUID id, @RequestBody UpdateFuelStatusRequest request) {
        return FuelResponse.from(updateFuelStatusUseCase.execute(id, request));
    }

    @GetMapping
    public List<FuelResponse> list() {
        return listFuelsUseCase.execute().stream()
            .map(FuelResponse::from)
            .toList();
    }
}
