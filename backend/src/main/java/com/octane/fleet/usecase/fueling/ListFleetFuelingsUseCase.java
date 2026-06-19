package com.octane.fleet.usecase.fueling;

import com.octane.fleet.domain.repository.FleetFuelingRepository;
import com.octane.fleet.usecase.FleetFuelingResponse;
import com.octane.shared.pagination.PageResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class ListFleetFuelingsUseCase {

    private final FleetFuelingRepository fleetFuelingRepository;

    public ListFleetFuelingsUseCase(FleetFuelingRepository fleetFuelingRepository) {
        this.fleetFuelingRepository = fleetFuelingRepository;
    }

    public PageResponse<FleetFuelingResponse> execute(UUID stationId, UUID clientId, UUID vehicleId,
                                                       UUID driverId, LocalDate from, LocalDate to,
                                                       int page, int size) {
        return fleetFuelingRepository.findByFilters(stationId, clientId, vehicleId, driverId, from, to, page, size)
                .map(FleetFuelingResponse::from);
    }
}
