package com.predicto.auth;

import com.predicto.auth.dto.LoginRequest;
import com.predicto.auth.dto.RegisterRequest;
import com.predicto.auth.security.JwtUser;
import com.predicto.auth.security.JwtUtil;
import com.predicto.common.enums.Game;
import com.predicto.common.enums.UserRole;
import com.predicto.email.EmailService;
import com.predicto.rank.RankService;
import com.predicto.season.RankTier;
import com.predicto.wallet.Wallet;
import com.predicto.wallet.WalletRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RankService rankService;
    private final EmailService emailService;

    @Value("${app.cookie.secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        String username = req.getUsername().trim().toLowerCase();
        if (!username.matches("^[a-z0-9_-]+$")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid username format"));
        }
        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Username already taken"));
        }
        var user = User.builder()
                .username(username)
                .displayName(req.getUsername().trim())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .role(UserRole.USER)
                .build();
        user = userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }

        walletRepository.save(Wallet.builder().user(user).lolElo(100).cs2Elo(100).build());

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        ResponseCookie cookie = ResponseCookie.from("predicto_token", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("userId", user.getId(), "username", user.getUsername(), "role", user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        var user = userRepository.findByUsername(req.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole().name());
        ResponseCookie cookie = ResponseCookie.from("predicto_token", token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ofDays(7))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("userId", user.getId(), "username", user.getUsername(), "role", user.getRole().name()));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("predicto_token", "")
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out"));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        var user = userRepository.findById(jwtUser.id()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        var wallet = walletRepository.findByUserId(user.getId()).orElse(null);
        RankTier lolRank = wallet != null
                ? rankService.resolveRank(Game.LOL, wallet.getLolElo())
                : null;
        RankTier cs2Rank = wallet != null
                ? rankService.resolveRank(Game.CS2, wallet.getCs2Elo())
                : null;
        Map<String, Object> body = new HashMap<>();
        body.put("id", user.getId());
        body.put("username", user.getUsername());
        body.put("displayName", user.getDisplayName());
        body.put("avatarUrl", user.getAvatarUrl());
        body.put("email", user.getEmail());
        body.put("preferredGame", user.getPreferredGame());
        body.put("badge", user.getBadge());
        body.put("role", user.getRole().name());
        if (wallet != null) {
            body.put("wallet", Map.of(
                    "balance", wallet.getBalance(),
                    "lolElo", wallet.getLolElo(),
                    "cs2Elo", wallet.getCs2Elo(),
                    "lifetimeWageredLol", wallet.getLifetimeWageredLol(),
                    "lifetimeWageredCs2", wallet.getLifetimeWageredCs2()
            ));
        }
        body.put("lolRank", lolRank != null ? lolRank.getTierName() : null);
        body.put("cs2Rank", cs2Rank != null ? cs2Rank.getTierName() : null);
        return ResponseEntity.ok(body);
    }
}
