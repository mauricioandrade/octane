package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetClient;
import com.octane.fleet.domain.repository.FleetClientRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public class FleetClientRepositoryImpl implements FleetClientRepository {

    private final FleetClientJpaRepository jpaRepository;

    public FleetClientRepositoryImpl(FleetClientJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FleetClient save(FleetClient client) {
        return jpaRepository.save(client);
    }

    @Override
    public Optional<FleetClient> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<FleetClient> findByStationId(UUID stationId, Boolean active) {
        if (active == null) {
            return jpaRepository.findByStation_Id(stationId);
        }
        return jpaRepository.findByStation_IdAndActive(stationId, active);
    }

    @Override
    public Optional<FleetClient> findByCnpjAndStationId(String cnpj, UUID stationId) {
        return jpaRepository.findByCnpjAndStation_Id(cnpj, stationId);
    }

    @Override
    public BigDecimal sumCurrentMonthSpend(UUID clientId) {
        return jpaRepository.sumCurrentMonthSpend(clientId);
    }

    @Override
    public Map<UUID, BigDecimal> sumCurrentMonthSpendByClientIds(List<UUID> clientIds) {
        if (clientIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return jpaRepository.sumCurrentMonthSpendByClientIds(clientIds).stream()
                .collect(Collectors.toMap(
                        ClientSpendProjection::getClientId,
                        ClientSpendProjection::getSpend));
    }
}
