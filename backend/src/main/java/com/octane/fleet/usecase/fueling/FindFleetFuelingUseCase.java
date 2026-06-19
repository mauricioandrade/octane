package com.octane.fleet.usecase.fueling;

import com.octane.fleet.domain.repository.FleetFuelingRepository;
import com.octane.fleet.usecase.FleetFuelingResponse;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FindFleetFuelingUseCase {

    private final FleetFuelingRepository fleetFuelingRepository;

    public FindFleetFuelingUseCase(FleetFuelingRepository fleetFuelingRepository) {
        this.fleetFuelingRepository = fleetFuelingRepository;
    }

    public FleetFuelingResponse execute(UUID id) {
        var ff = fleetFuelingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Abastecimento de frota não encontrado: " + id));
        return FleetFuelingResponse.from(ff);
    }
}
