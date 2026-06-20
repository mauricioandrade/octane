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

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FindUserUseCase sut;

    @Test
    void execute_returnsUser_whenFound() {
        var id = UUID.randomUUID();
        var now = LocalDateTime.now();
        var user = new User(id, "joao", "hash", "João", UserRole.ATTENDANT, true, now, now);

        when(userRepository.findById(id)).thenReturn(Optional.of(user));

        var result = sut.execute(id);

        assertThat(result.id()).isEqualTo(id);
        assertThat(result.username()).isEqualTo("joao");
    }

    @Test
    void execute_throwsEntityNotFound_whenNotFound() {
        var id = UUID.randomUUID();

        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.execute(id))
                .isInstanceOf(EntityNotFoundException.class);
    }
}
