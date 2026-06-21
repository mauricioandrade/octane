package com.octane.user.usecase;

import com.octane.audit.usecase.AuditService;
import com.octane.shared.exception.BusinessException;
import com.octane.shared.exception.EntityNotFoundException;
import com.octane.user.domain.UserRole;
import com.octane.user.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UpdateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder,
                             AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse execute(UUID id, UpdateUserRequest request) {
        var user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        if (request.name() != null && !request.name().isBlank()) {
            user.setName(request.name());
        }

        if (request.role() != null) {
            try {
                user.setRole(UserRole.valueOf(request.role()));
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Perfil inválido: " + request.role());
            }
        }

        if (request.active() != null) {
            user.setActive(request.active());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }

        user.setUpdatedAt(LocalDateTime.now());
        user = userRepository.save(user);
        auditService.log("UPDATE", "User", user.getId(), user.getUsername());

        return UserResponse.from(user);
    }
}
