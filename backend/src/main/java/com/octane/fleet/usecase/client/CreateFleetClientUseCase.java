package com.octane.fleet.usecase.client;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.usecase.FleetClientResponse;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateFleetClientUseCase {

    private final FleetClientRepository fleetClientRepository;
    private final StationRepository stationRepository;

    public CreateFleetClientUseCase(FleetClientRepository fleetClientRepository,
                                    StationRepository stationRepository) {
        this.fleetClientRepository = fleetClientRepository;
        this.stationRepository = stationRepository;
    }

    @Transactional
    public FleetClientResponse execute(CreateFleetClientRequest request) {
        var stationId = request.stationId();
        var station = stationRepository.findById(stationId)
                .orElseThrow(() -> new EntityNotFoundException("Posto não encontrado: " + stationId));

        fleetClientRepository.findByCnpjAndStationId(request.cnpj(), stationId)
                .ifPresent(c -> { throw new BusinessException("CNPJ já cadastrado neste posto"); });

        var now = LocalDateTime.now();
        var client = new FleetClient(null, station, request.cnpj(), request.companyName(),
                request.tradeName(), request.monthlyLimit(), true, now);
        client = fleetClientRepository.save(client);

        return toResponse(client, java.math.BigDecimal.ZERO);
    }

    public static FleetClientResponse toResponse(FleetClient client, java.math.BigDecimal currentMonthSpend) {
        return new FleetClientResponse(
                client.getId(),
                client.getStation().getId(),
                client.getCnpj(),
                client.getCompanyName(),
                client.getTradeName(),
                client.getMonthlyLimit(),
                currentMonthSpend,
                client.isActive(),
                client.getCreatedAt()
        );
    }
}
