package com.octane.fueling.repository;

import com.octane.fueling.domain.Shift;
import com.octane.fueling.domain.ShiftStatus;
import com.octane.fueling.domain.repository.ShiftRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class ShiftRepositoryImpl implements ShiftRepository {

    private final ShiftJpaRepository jpaRepository;

    public ShiftRepositoryImpl(ShiftJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Shift save(Shift shift) {
        return jpaRepository.save(shift);
    }

    @Override
    public Optional<Shift> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Shift> findOpenByStationId(UUID stationId) {
        return jpaRepository.findByStation_IdAndStatus(stationId, ShiftStatus.OPEN);
    }

    @Override
    public List<Shift> findByStationId(UUID stationId) {
        return jpaRepository.findByStation_Id(stationId);
    }
}
