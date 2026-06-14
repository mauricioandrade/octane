package com.octane.fleet.domain.repository;

import com.octane.fleet.domain.FleetClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FleetClientRepository {
    FleetClient save(FleetClient client);
    Optional<FleetClient> findById(UUID id);
    List<FleetClient> findByStationId(UUID stationId, Boolean active);
    Optional<FleetClient> findByCnpjAndStationId(String cnpj, UUID stationId);
    BigDecimal sumCurrentMonthSpend(UUID clientId);
}
