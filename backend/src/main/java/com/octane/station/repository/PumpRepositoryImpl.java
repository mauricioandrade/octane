package com.octane.station.repository;

import com.octane.station.domain.Pump;
import com.octane.station.domain.PumpStatus;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class PumpRepositoryImpl implements PumpRepository {

    private final PumpJpaRepository jpaRepository;

    public PumpRepositoryImpl(PumpJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Pump save(Pump pump) {
        return jpaRepository.save(pump);
    }

    @Override
    public Optional<Pump> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Pump> findByStationId(UUID stationId) {
        return jpaRepository.findByStation_Id(stationId);
    }

    @Override
    public List<Pump> findByStationId(UUID stationId, PumpStatus status) {
        if (status != null) {
            return jpaRepository.findByStation_IdAndStatus(stationId, status);
        }
        return jpaRepository.findByStation_Id(stationId);
    }

    @Override
    public boolean existsByStationIdAndNumber(UUID stationId, int number) {
        return jpaRepository.existsByStation_IdAndNumber(stationId, number);
    }
}
