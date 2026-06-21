package com.octane.fleet.usecase.driver;

import com.octane.audit.usecase.AuditService;
import com.octane.fleet.domain.FleetDriver;
import com.octane.fleet.domain.repository.FleetClientRepository;
import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.usecase.FleetDriverResponse;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateFleetDriverUseCase {

    private final FleetDriverRepository fleetDriverRepository;
    private final FleetClientRepository fleetClientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public CreateFleetDriverUseCase(FleetDriverRepository fleetDriverRepository,
                                    FleetClientRepository fleetClientRepository,
                                    PasswordEncoder passwordEncoder,
                                    AuditService auditService) {
        this.fleetDriverRepository = fleetDriverRepository;
        this.fleetClientRepository = fleetClientRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public FleetDriverResponse execute(CreateFleetDriverRequest request) {
        if (request.pin() == null && request.rfidTag() == null) {
            throw new BusinessException("Informe PIN ou RFID");
        }

        var clientId = request.clientId();
        var client = fleetClientRepository.findById(clientId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente de frota não encontrado: " + clientId));

        fleetDriverRepository.findByCpfAndClientId(request.cpf(), clientId)
                .ifPresent(d -> { throw new BusinessException("CPF já cadastrado para este cliente"); });

        String pinHash = request.pin() != null ? passwordEncoder.encode(request.pin()) : null;

        var driver = new FleetDriver(null, client, request.name(), request.cpf(),
                pinHash, request.rfidTag(), true, LocalDateTime.now());
        driver = fleetDriverRepository.save(driver);
        auditService.log("CREATE", "FleetDriver", driver.getId(), driver.getName());
        return FleetDriverResponse.from(driver);
    }
}
