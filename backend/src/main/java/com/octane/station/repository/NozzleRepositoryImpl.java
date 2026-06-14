package com.octane.station.repository;

import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class NozzleRepositoryImpl implements NozzleRepository {

    private final NozzleJpaRepository jpaRepository;

    public NozzleRepositoryImpl(NozzleJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Nozzle save(Nozzle nozzle) {
        return jpaRepository.save(nozzle);
    }

    @Override
    public Optional<Nozzle> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Nozzle> findByPumpId(UUID pumpId) {
        return jpaRepository.findByPump_Id(pumpId);
    }

    @Override
    public List<Nozzle> findByPumpId(UUID pumpId, boolean active) {
        return jpaRepository.findByPump_IdAndActive(pumpId, active);
    }

    @Override
    public boolean existsByPumpIdAndNumber(UUID pumpId, int number) {
        return jpaRepository.existsByPump_IdAndNumber(pumpId, number);
    }
}
