package com.octane.user.repository;

import com.octane.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface UserJpaRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    @Query("SELECT s.id FROM User u JOIN u.stations s WHERE u.id = :userId")
    List<UUID> findStationIdsByUserId(UUID userId);
}
