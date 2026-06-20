package com.octane.user.usecase;

import com.octane.shared.exception.EntityNotFoundException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdateUserUseCase sut;

    @Test
    void execute_updatesNameAndRole() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var user = new User(id, "joao", "hash", "João", UserRole.ATTENDANT, true, now, now);
        var request = new UpdateUserRequest("João Atualizado", "MANAGER", null, null);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        var result = sut.execute(id, request);

        assertThat(result.name()).isEqualTo("João Atualizado");
        assertThat(result.role()).isEqualTo("MANAGER");
        verify(userRepository).save(user);
    }

    @Test
    void execute_throwsEntityNotFound_whenUserNotFound() {
        var id = UUID.randomUUID();
        var request = new UpdateUserRequest("Nome", null, null, null);

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id, request))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void execute_updatesPassword_whenProvided() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var user = new User(id, "joao", "old_hash", "João", UserRole.ATTENDANT, true, now, now);
        var request = new UpdateUserRequest(null, null, null, "newpass123");

        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass123")).thenReturn("new_hash");
        when(userRepository.save(any(User.class))).thenReturn(user);

        sut.execute(id, request);

        assertThat(user.getPasswordHash()).isEqualTo("new_hash");
        verify(passwordEncoder).encode("newpass123");
    }
}
