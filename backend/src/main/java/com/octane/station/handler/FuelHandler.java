package com.octane.station.handler;

import com.octane.station.usecase.fuel.ListFuelsUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fuels")
public class FuelHandler {

    private final ListFuelsUseCase listFuelsUseCase;

    public FuelHandler(ListFuelsUseCase listFuelsUseCase) {
        this.listFuelsUseCase = listFuelsUseCase;
    }

    @GetMapping
    public List<FuelResponse> list() {
        return listFuelsUseCase.execute().stream()
            .map(FuelResponse::from)
            .toList();
    }
}
