package com.octane.shared.auth;

import com.octane.user.domain.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticatedUserService {

    private final UserRepository userRepository;

    public AuthenticatedUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public AuthenticatedUser getCurrentUser() {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        var user = userRepository.findByUsername(username).orElseThrow();
        var stationIds = userRepository.findStationIdsByUserId(user.getId());
        return new AuthenticatedUser(user.getId(), user.getUsername(), user.getRole(), stationIds);
    }
}
