package com.octane.station.usecase.fuel;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListFuelsUseCase {

    private final FuelRepository fuelRepository;

    public ListFuelsUseCase(FuelRepository fuelRepository) {
        this.fuelRepository = fuelRepository;
    }

    public List<Fuel> execute() {
        return fuelRepository.findAll();
    }
}
