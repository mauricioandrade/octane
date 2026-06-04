package com.octane.station.usecase.nozzle;

import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.NozzleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ListNozzlesByPumpUseCase {

    private final NozzleRepository nozzleRepository;

    public ListNozzlesByPumpUseCase(NozzleRepository nozzleRepository) {
        this.nozzleRepository = nozzleRepository;
    }

    public List<Nozzle> execute(UUID pumpId) {
        return nozzleRepository.findByPumpId(pumpId);
    }
}
