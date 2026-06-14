package com.octane.fleet.usecase.driver;

import com.octane.fleet.domain.repository.FleetDriverRepository;
import com.octane.fleet.usecase.FleetDriverResponse;
import com.octane.shared.exception.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UpdateFleetDriverUseCase {

    private final FleetDriverRepository fleetDriverRepository;
    private final PasswordEncoder passwordEncoder;

    public UpdateFleetDriverUseCase(FleetDriverRepository fleetDriverRepository,
                                    PasswordEncoder passwordEncoder) {
        this.fleetDriverRepository = fleetDriverRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public FleetDriverResponse execute(UUID id, UpdateFleetDriverRequest request) {
        var driver = fleetDriverRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Motorista não encontrado: " + id));

        if (request.name() != null) driver.setName(request.name());
        if (request.pin() != null) driver.setPinHash(passwordEncoder.encode(request.pin()));
        if (request.rfidTag() != null) driver.setRfidTag(request.rfidTag());
        if (request.active() != null) driver.setActive(request.active());

        driver = fleetDriverRepository.save(driver);
        return CreateFleetDriverUseCase.toResponse(driver);
    }
}
