package com.octane.user.usecase;

import com.octane.user.domain.User;
import com.octane.user.domain.UserRole;
import com.octane.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ListUsersUseCase sut;

    @Test
    void execute_returnsAllUsers() {
        var now = LocalDateTime.now();
        var users = List.of(
                new User(UUID.randomUUID(), "admin", "h1", "Admin", UserRole.ADMIN, true, now, now),
                new User(UUID.randomUUID(), "joao", "h2", "João", UserRole.ATTENDANT, true, now, now)
        );

        when(userRepository.findAll()).thenReturn(users);

        var result = sut.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).username()).isEqualTo("admin");
        assertThat(result.get(1).role()).isEqualTo("ATTENDANT");
    }
}
