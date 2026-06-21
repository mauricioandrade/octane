package com.octane.user.usecase;

import com.octane.user.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ListUserStationsUseCase {

    private final UserRepository userRepository;

    public ListUserStationsUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<UUID> execute(UUID userId) {
        return userRepository.findStationIdsByUserId(userId);
    }
}
