package com.octane.user.usecase;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.station.domain.repository.StationRepository;
import com.octane.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class UpdateUserStationsUseCase {

    private final UserRepository userRepository;
    private final StationRepository stationRepository;
    private final AuditService auditService;

    public UpdateUserStationsUseCase(UserRepository userRepository,
                                     StationRepository stationRepository,
                                     AuditService auditService) {
        this.userRepository = userRepository;
        this.stationRepository = stationRepository;
        this.auditService = auditService;
    }

    @Transactional
    public List<UUID> execute(UUID userId, UpdateUserStationsRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + userId));

        var stations = stationRepository.findAllById(request.stationIds());
        if (stations.size() != request.stationIds().size()) {
            throw new EntityNotFoundException("Um ou mais postos não encontrados");
        }

        user.setStations(new HashSet<>(stations));
        userRepository.save(user);

        auditService.log("UPDATE_STATIONS", "User", userId,
                "Postos atualizados: " + request.stationIds());

        return stations.stream().map(s -> s.getId()).toList();
    }
}
