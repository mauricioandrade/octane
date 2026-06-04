package com.octane.fueling.repository;

import com.octane.fueling.domain.Fueling;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

interface FuelingJpaRepository extends JpaRepository<Fueling, UUID> {
    List<Fueling> findByShift_Id(UUID shiftId);
}
