package com.octane.user.usecase;

import com.octane.shared.exception.BusinessException;
import com.octane.user.domain.User;
import com.octane.user.domain.UserRole;
import com.octane.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserUseCase sut;

    @Test
    void execute_createsUser_whenUsernameIsNew() {
        var request = new CreateUserRequest("joao", "senha123", "João Silva", "ATTENDANT");
        var now = LocalDateTime.now();
        var saved = new User(UUID.randomUUID(), "joao", "hashed", "João Silva",
                UserRole.ATTENDANT, true, now, now);

        when(userRepository.findByUsername("joao")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("senha123")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        var result = sut.execute(request);

        assertThat(result.username()).isEqualTo("joao");
        assertThat(result.name()).isEqualTo("João Silva");
        assertThat(result.role()).isEqualTo("ATTENDANT");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void execute_throwsBusinessException_whenUsernameExists() {
        var request = new CreateUserRequest("joao", "senha123", "João Silva", "ATTENDANT");
        var existing = new User(UUID.randomUUID(), "joao", "hash", "Outro",
                UserRole.ATTENDANT, true, LocalDateTime.now(), LocalDateTime.now());

        when(userRepository.findByUsername("joao")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("já existe");

        verify(userRepository, never()).save(any());
    }

    @Test
    void execute_throwsBusinessException_whenRoleIsInvalid() {
        var request = new CreateUserRequest("joao", "senha123", "João Silva", "INVALID_ROLE");

        when(userRepository.findByUsername("joao")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Perfil inválido");

        verify(userRepository, never()).save(any());
    }
}
