package com.octane.inventory.repository;

import com.octane.inventory.domain.Tank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface TankJpaRepository extends JpaRepository<Tank, UUID> {
    List<Tank> findByStation_IdOrderByNameAsc(UUID stationId);
}
