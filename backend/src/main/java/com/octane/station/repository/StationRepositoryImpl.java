package com.octane.station.repository;

import com.octane.station.domain.Station;
import com.octane.station.domain.repository.StationRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class StationRepositoryImpl implements StationRepository {

    private final StationJpaRepository jpaRepository;

    public StationRepositoryImpl(StationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Station save(Station station) {
        return jpaRepository.save(station);
    }

    @Override
    public Optional<Station> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Station> findByCnpj(String cnpj) {
        return jpaRepository.findByCnpj(cnpj);
    }

    @Override
    public List<Station> findAll() {
        return jpaRepository.findAll();
    }
}
