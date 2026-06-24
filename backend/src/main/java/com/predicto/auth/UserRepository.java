package com.predicto.auth;

import com.predicto.common.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByDiscordId(String discordId);

    List<User> findByRole(UserRole role);

    Page<User> findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(String username, String displayName, Pageable pageable);
}
