package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetClient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface ClientSpendProjection {
    UUID getClientId();
    BigDecimal getSpend();
}

interface FleetClientJpaRepository extends JpaRepository<FleetClient, UUID> {

    List<FleetClient> findByStation_Id(UUID stationId);

    List<FleetClient> findByStation_IdAndActive(UUID stationId, boolean active);

    Optional<FleetClient> findByCnpjAndStation_Id(String cnpj, UUID stationId);

    @Query("SELECT COALESCE(SUM(f.totalAmount), 0) FROM FleetFueling ff " +
           "JOIN ff.fueling f " +
           "WHERE ff.vehicle.client.id = :clientId " +
           "AND FUNCTION('DATE_TRUNC', 'month', f.fueledAt) = FUNCTION('DATE_TRUNC', 'month', CURRENT_TIMESTAMP) " +
           "AND f.status = com.octane.fueling.domain.FuelingStatus.ACTIVE")
    BigDecimal sumCurrentMonthSpend(@Param("clientId") UUID clientId);

    @Query("SELECT ff.vehicle.client.id AS clientId, COALESCE(SUM(f.totalAmount), 0) AS spend " +
           "FROM FleetFueling ff " +
           "JOIN ff.fueling f " +
           "WHERE ff.vehicle.client.id IN :clientIds " +
           "AND FUNCTION('DATE_TRUNC', 'month', f.fueledAt) = FUNCTION('DATE_TRUNC', 'month', CURRENT_TIMESTAMP) " +
           "AND f.status = com.octane.fueling.domain.FuelingStatus.ACTIVE " +
           "GROUP BY ff.vehicle.client.id")
    List<ClientSpendProjection> sumCurrentMonthSpendByClientIds(@Param("clientIds") List<UUID> clientIds);
}
