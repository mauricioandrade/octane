package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetDriver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FleetDriverJpaRepository extends JpaRepository<FleetDriver, UUID> {

    List<FleetDriver> findByClient_Id(UUID clientId);

    List<FleetDriver> findByClient_IdAndActive(UUID clientId, boolean active);

    Optional<FleetDriver> findByCpfAndClient_Id(String cpf, UUID clientId);

    @Query("SELECT d FROM FleetDriver d WHERE d.rfidTag = :rfidTag AND d.active = true " +
           "AND d.client.station.id = :stationId")
    List<FleetDriver> findByRfidTagAndStationId(@Param("rfidTag") String rfidTag,
                                                @Param("stationId") UUID stationId);

    @Query("SELECT d FROM FleetDriver d WHERE d.cpf = :cpf AND d.active = true " +
           "AND d.client.station.id = :stationId")
    List<FleetDriver> findByCpfAndStationId(@Param("cpf") String cpf,
                                            @Param("stationId") UUID stationId);
}
