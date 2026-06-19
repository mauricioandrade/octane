package com.octane.station.handler;

import com.octane.station.usecase.fuel.FuelResponse;
import com.octane.station.usecase.fuel.CreateFuelRequest;
import com.octane.station.usecase.fuel.CreateFuelUseCase;
import com.octane.station.usecase.fuel.ListFuelsUseCase;
import com.octane.station.usecase.fuel.UpdateFuelRequest;
import com.octane.station.usecase.fuel.UpdateFuelStatusRequest;
import com.octane.station.usecase.fuel.UpdateFuelStatusUseCase;
import com.octane.station.usecase.fuel.UpdateFuelUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fuels")
public class FuelHandler {

    private final ListFuelsUseCase listFuelsUseCase;
    private final UpdateFuelStatusUseCase updateFuelStatusUseCase;
    private final CreateFuelUseCase createFuelUseCase;
    private final UpdateFuelUseCase updateFuelUseCase;

    public FuelHandler(ListFuelsUseCase listFuelsUseCase,
                       UpdateFuelStatusUseCase updateFuelStatusUseCase,
                       CreateFuelUseCase createFuelUseCase,
                       UpdateFuelUseCase updateFuelUseCase) {
        this.listFuelsUseCase = listFuelsUseCase;
        this.updateFuelStatusUseCase = updateFuelStatusUseCase;
        this.createFuelUseCase = createFuelUseCase;
        this.updateFuelUseCase = updateFuelUseCase;
    }

    @GetMapping
    public List<FuelResponse> list() {
        return listFuelsUseCase.execute().stream()
            .map(FuelResponse::from)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FuelResponse create(@Valid @RequestBody CreateFuelRequest request) {
        return FuelResponse.from(createFuelUseCase.execute(request));
    }

    @PutMapping("/{id}")
    public FuelResponse update(@PathVariable UUID id, @Valid @RequestBody UpdateFuelRequest request) {
        return FuelResponse.from(updateFuelUseCase.execute(id, request));
    }

    @PatchMapping("/{id}/status")
    public FuelResponse updateStatus(@PathVariable UUID id, @RequestBody UpdateFuelStatusRequest request) {
        return FuelResponse.from(updateFuelStatusUseCase.execute(id, request));
    }
}
