package com.octane.station.usecase.nozzle;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.Nozzle;
import com.octane.station.domain.repository.FuelRepository;
import com.octane.station.domain.repository.NozzleRepository;
import com.octane.station.domain.repository.PumpRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CreateNozzleUseCase {

    private final PumpRepository pumpRepository;
    private final FuelRepository fuelRepository;
    private final NozzleRepository nozzleRepository;
    private final AuditService auditService;

    public CreateNozzleUseCase(PumpRepository pumpRepository, FuelRepository fuelRepository,
                               NozzleRepository nozzleRepository, AuditService auditService) {
        this.pumpRepository = pumpRepository;
        this.fuelRepository = fuelRepository;
        this.nozzleRepository = nozzleRepository;
        this.auditService = auditService;
    }

    @Transactional
    public Nozzle execute(UUID pumpId, CreateNozzleRequest request) {
        var pump = pumpRepository.findById(pumpId)
            .orElseThrow(() -> new EntityNotFoundException("Bomba não encontrada: " + pumpId));

        var fuel = fuelRepository.findById(request.fuelId())
            .orElseThrow(() -> new EntityNotFoundException("Combustível não encontrado: " + request.fuelId()));

        if (nozzleRepository.existsByPumpIdAndNumber(pumpId, request.number())) {
            throw new BusinessException("Bico número " + request.number() + " já existe nesta bomba");
        }

        var now = LocalDateTime.now();
        var nozzle = new Nozzle(null, request.number(), pump, fuel, true, now, now);
        var saved = nozzleRepository.save(nozzle);
        auditService.log("CREATE", "Nozzle", saved.getId(), "bico " + saved.getNumber());
        return saved;
    }
}
