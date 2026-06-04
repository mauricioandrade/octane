package com.octane.station.repository;

import com.octane.station.domain.Fuel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface FuelJpaRepository extends JpaRepository<Fuel, UUID> {}
