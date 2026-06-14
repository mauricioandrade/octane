package com.octane.station.repository;

import com.octane.station.domain.Fuel;
import com.octane.station.domain.repository.FuelRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class FuelRepositoryImpl implements FuelRepository {

    private final FuelJpaRepository jpaRepository;

    public FuelRepositoryImpl(FuelJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Fuel save(Fuel fuel) {
        return jpaRepository.save(fuel);
    }

    @Override
    public Optional<Fuel> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Fuel> findByName(String name) {
        return jpaRepository.findByName(name);
    }

    @Override
    public List<Fuel> findAll() {
        return jpaRepository.findAll();
    }
}
