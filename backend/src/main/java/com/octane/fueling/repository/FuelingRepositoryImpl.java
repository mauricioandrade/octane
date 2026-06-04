package com.octane.fueling.repository;

import com.octane.fueling.domain.Fueling;
import com.octane.fueling.domain.repository.FuelingRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FuelingRepositoryImpl implements FuelingRepository {

    private final FuelingJpaRepository jpaRepository;

    public FuelingRepositoryImpl(FuelingJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Fueling save(Fueling fueling) {
        return jpaRepository.save(fueling);
    }

    @Override
    public List<Fueling> findByShiftId(UUID shiftId) {
        return jpaRepository.findByShift_Id(shiftId);
    }

    @Override
    public Optional<Fueling> findById(UUID id) {
        return jpaRepository.findById(id);
    }
}
