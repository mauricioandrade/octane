package com.octane.fleet.repository;

import com.octane.fleet.domain.FleetFueling;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface FleetFuelingJpaRepository extends JpaRepository<FleetFueling, UUID> {

    @Query("SELECT MAX(ff.odometer) FROM FleetFueling ff " +
           "JOIN ff.fueling f " +
           "WHERE ff.vehicle.id = :vehicleId AND f.status = com.octane.fueling.domain.FuelingStatus.ACTIVE")
    Optional<Integer> findLastOdometerByVehicleId(@Param("vehicleId") UUID vehicleId);

    @Query("SELECT ff FROM FleetFueling ff " +
           "JOIN ff.fueling f " +
           "JOIN ff.vehicle v " +
           "JOIN v.client c " +
           "WHERE c.station.id = :stationId " +
           "AND (:clientId IS NULL OR c.id = :clientId) " +
           "AND (:vehicleId IS NULL OR v.id = :vehicleId) " +
           "AND (:driverId IS NULL OR ff.driver.id = :driverId) " +
           "AND (:from IS NULL OR CAST(f.fueledAt AS date) >= :from) " +
           "AND (:to IS NULL OR CAST(f.fueledAt AS date) <= :to) " +
           "AND f.status = com.octane.fueling.domain.FuelingStatus.ACTIVE")
    Page<FleetFueling> findByFilters(@Param("stationId") UUID stationId,
                                      @Param("clientId") UUID clientId,
                                      @Param("vehicleId") UUID vehicleId,
                                      @Param("driverId") UUID driverId,
                                      @Param("from") LocalDate from,
                                      @Param("to") LocalDate to,
                                      Pageable pageable);

    @Query("SELECT ff FROM FleetFueling ff " +
           "JOIN ff.fueling f " +
           "JOIN ff.vehicle v " +
           "JOIN v.client c " +
           "WHERE c.station.id = :stationId " +
           "AND (:clientId IS NULL OR c.id = :clientId) " +
           "AND (:vehicleId IS NULL OR v.id = :vehicleId) " +
           "AND (:driverId IS NULL OR ff.driver.id = :driverId) " +
           "AND (:from IS NULL OR CAST(f.fueledAt AS date) >= :from) " +
           "AND (:to IS NULL OR CAST(f.fueledAt AS date) <= :to) " +
           "AND f.status = com.octane.fueling.domain.FuelingStatus.ACTIVE " +
           "ORDER BY f.fueledAt DESC")
    List<FleetFueling> findAllByFilters(@Param("stationId") UUID stationId,
                                         @Param("clientId") UUID clientId,
                                         @Param("vehicleId") UUID vehicleId,
                                         @Param("driverId") UUID driverId,
                                         @Param("from") LocalDate from,
                                         @Param("to") LocalDate to);
}
