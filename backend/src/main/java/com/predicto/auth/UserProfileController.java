package com.predicto.auth;

import com.predicto.auth.security.JwtUser;
import com.predicto.betting.BetRepository;
import com.predicto.common.enums.Game;
import com.predicto.rank.RankService;
import com.predicto.season.RankTier;
import com.predicto.wallet.WalletRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final BetRepository betRepository;
    private final RankService rankService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    @Transactional
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal JwtUser jwtUser) {
        if (jwtUser == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(jwtUser.id()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        var wallet = walletRepository.findByUserId(user.getId()).orElse(null);

        var bets = betRepository.findByUserId(user.getId());
        long totalBets = bets.size();
        long wonBets = bets.stream().filter(b -> b.getPointsAwarded() > 0).count();
        double winRate = totalBets > 0 ? Math.round((double) wonBets / totalBets * 1000.0) / 10.0 : 0.0;

        var recentBets = bets.stream()
                .sorted(Comparator.comparing(b -> b.getMatch().getStartsAt(), Comparator.reverseOrder()))
                .limit(5)
                .map(b -> {
                    var m = new LinkedHashMap<String, Object>();
                    m.put("id", b.getId());
                    m.put("matchTitle", b.getMatch().getTeamA().getName() + " vs " + b.getMatch().getTeamB().getName());
                    m.put("game", b.getMatch().getGame());
                    m.put("predictedWinner", b.getWinnerTeam() != null ? b.getWinnerTeam().getName() : null);
                    m.put("stake", b.getStake());
                    m.put("pointsAwarded", b.getPointsAwarded());
                    m.put("status", b.getStatus());
                    m.put("matchDate", b.getMatch().getStartsAt());
                    return m;
                }).toList();

        var map = new LinkedHashMap<String, Object>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("displayName", user.getDisplayName());
        map.put("avatarUrl", user.getAvatarUrl());
        map.put("badge", user.getBadge());
        map.put("createdAt", user.getCreatedAt());
        map.put("balance", wallet != null ? wallet.getBalance() : 0);
        map.put("lolElo", wallet != null ? wallet.getLolElo() : 0);
        map.put("cs2Elo", wallet != null ? wallet.getCs2Elo() : 0);
        map.put("totalPredictions", totalBets);
        map.put("wonPredictions", wonBets);
        map.put("winRate", winRate);
        map.put("recentBets", recentBets);
        return ResponseEntity.ok(map);
    }

    @PatchMapping
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal JwtUser jwtUser,
                                           @RequestBody UpdateProfileRequest req) {
        if (jwtUser == null) {
            return ResponseEntity.status(401).build();
        }
        var user = userRepository.findById(jwtUser.id()).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (req.getUsername() != null) {
            if (userRepository.findByUsername(req.getUsername()).filter(u -> !u.getId().equals(user.getId())).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username already taken"));
            }
            user.setUsername(req.getUsername());
        }
        if (req.getDisplayName() != null) {
            user.setDisplayName(req.getDisplayName());
        }
        if (req.getPreferredGame() != null) {
            if (!req.getPreferredGame().equals("LOL") && !req.getPreferredGame().equals("CS2")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Invalid preferredGame. Must be 'LOL' or 'CS2'"));
            }
            user.setPreferredGame(req.getPreferredGame());
        }

        userRepository.save(user);

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

    @PutMapping("/password")
    @Transactional
    public ResponseEntity<?> changePassword(@AuthenticationPrincipal JwtUser jwtUser,
                                            @Valid @RequestBody ChangePasswordRequest req) {
        if (jwtUser == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(jwtUser.id()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (user.getPasswordHash() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Cannot change password for Discord-linked accounts"));
        }
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPasswordHash())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Current password is incorrect"));
        }
        user.setPasswordHash(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Password updated"));
    }

    @PutMapping("/email")
    @Transactional
    public ResponseEntity<?> changeEmail(@AuthenticationPrincipal JwtUser jwtUser,
                                         @Valid @RequestBody ChangeEmailRequest req) {
        if (jwtUser == null) return ResponseEntity.status(401).build();
        var user = userRepository.findById(jwtUser.id()).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        if (userRepository.findByEmail(req.getEmail()).filter(u -> !u.getId().equals(user.getId())).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email already in use"));
        }
        user.setEmail(req.getEmail());
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("message", "Email updated"));
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank private String currentPassword;
        @NotBlank @Size(min = 4, max = 100) private String newPassword;
    }

    @Data
    public static class ChangeEmailRequest {
        @NotBlank @Email private String email;
    }

    @Data
    public static class UpdateProfileRequest {
        private String username;
        private String displayName;
        @Pattern(regexp = "LOL|CS2", message = "Must be 'LOL' or 'CS2'")
        private String preferredGame;
    }
}
