package com.octane.user.usecase;

import com.octane.shared.exception.BusinessException;
import com.octane.user.domain.User;
import com.octane.user.domain.UserRole;
import com.octane.user.domain.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserUseCase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        userRepository.findByUsername(request.username())
                .ifPresent(u -> { throw new BusinessException("Nome de usuário já existe: " + request.username()); });

        UserRole role;
        try {
            role = UserRole.valueOf(request.role());
        } catch (IllegalArgumentException e) {
            throw new BusinessException("Perfil inválido: " + request.role());
        }

        var now = LocalDateTime.now();
        var user = new User(null, request.username(), passwordEncoder.encode(request.password()),
                request.name(), role, true, now, now);
        user = userRepository.save(user);

        return UserResponse.from(user);
    }
}
