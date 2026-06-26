package com.predicto.auth;

import com.predicto.auth.dto.RoleUpdateRequest;
import com.predicto.auth.dto.UpdateUserRequest;
import com.predicto.auth.dto.UserSummaryDto;
import com.predicto.auth.security.JwtUser;
import com.predicto.common.enums.UserRole;
import com.predicto.wallet.WalletRepository;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class UserManagementController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;

    @GetMapping
    public ResponseEntity<Page<UserSummaryDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        var pageable = PageRequest.of(page, size);
        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByUsernameContainingIgnoreCaseOrDisplayNameContainingIgnoreCase(search, search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        return ResponseEntity.ok(users.map(u -> {
            var wallet = walletRepository.findByUserId(u.getId()).orElse(null);
            return new UserSummaryDto(
                    u.getId(), u.getUsername(), u.getDisplayName(),
                    u.getEmail(), u.getRole().name(),
                    wallet != null ? wallet.getBalance() : 0,
                    u.getCreatedAt());
        }));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateRole(@PathVariable UUID id,
                                        @Valid @RequestBody RoleUpdateRequest req,
                                        @AuthenticationPrincipal JwtUser jwtUser) {
        if (id.equals(jwtUser.id())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot change your own role"));
        }

        var target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.notFound().build();
        }

        if (target.getRole() == UserRole.ADMIN) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot change another admin's role"));
        }

        UserRole newRole;
        try {
            newRole = UserRole.valueOf(req.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid role. Must be USER or EDITOR"));
        }

        if (newRole == UserRole.ADMIN) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot promote to ADMIN through this endpoint"));
        }

        target.setRole(newRole);
        userRepository.save(target);

        var wallet = walletRepository.findByUserId(target.getId()).orElse(null);
        var dto = new UserSummaryDto(
                target.getId(), target.getUsername(), target.getDisplayName(),
                target.getEmail(), target.getRole().name(),
                wallet != null ? wallet.getBalance() : 0,
                target.getCreatedAt());
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest req) {
        var target = userRepository.findById(id).orElse(null);
        if (target == null) {
            return ResponseEntity.notFound().build();
        }
        if (req.getUsername() != null) {
            target.setUsername(req.getUsername());
        }
        if (req.getEmail() != null) {
            target.setEmail(req.getEmail());
        }
        if (req.getRole() != null) {
            try {
                target.setRole(UserRole.valueOf(req.getRole().toUpperCase()));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid role"));
            }
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            target.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        }
        userRepository.save(target);
        var wallet = walletRepository.findByUserId(target.getId()).orElse(null);
        var dto = new UserSummaryDto(
                target.getId(), target.getUsername(), target.getDisplayName(),
                target.getEmail(), target.getRole().name(),
                wallet != null ? wallet.getBalance() : 0,
                target.getCreatedAt());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<String> deleteUser(@PathVariable UUID id) {
        try {
            entityManager.createNativeQuery("DELETE FROM pickban_predictions WHERE user_id = :id").setParameter("id", id).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM bets WHERE user_id = :id").setParameter("id", id).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM f1_predictions WHERE user_id = :id").setParameter("id", id).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM leaderboard_entries WHERE user_id = :id").setParameter("id", id).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM rewards WHERE user_id = :id").setParameter("id", id).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM wallets WHERE user_id = :id").setParameter("id", id).executeUpdate();
            entityManager.createNativeQuery("DELETE FROM users WHERE id = :id").setParameter("id", id).executeUpdate();
            return ResponseEntity.ok("Deleted: " + id);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage() + " | Cause: " + (e.getCause() != null ? e.getCause().getMessage() : "none"));
        }
    }
}
