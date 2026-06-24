package com.predicto.admin;

import com.predicto.auth.User;
import com.predicto.auth.UserRepository;
import com.predicto.auth.security.JwtUser;
import com.predicto.common.AuditLog;
import com.predicto.common.AuditLogRepository;
import com.predicto.common.enums.SeasonStatus;
import com.predicto.season.*;
import com.predicto.season.dto.CreateSeasonRequest;
import com.predicto.season.dto.SeasonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/seasons")
@RequiredArgsConstructor
public class SeasonAdminController {

    private final SeasonRepository seasonRepository;
    private final RewardRepository rewardRepository;
    private final LeaderboardService leaderboardService;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;

    @PostMapping
    public ResponseEntity<?> createSeason(@Valid @RequestBody CreateSeasonRequest request) {
        if (!request.startsAt().isBefore(request.endsAt())) {
            return ResponseEntity.badRequest().body(Map.of("message", "startsAt must be before endsAt"));
        }

        var existingActive = seasonRepository.findByGameAndStatus(request.game(), SeasonStatus.ACTIVE);
        if (!existingActive.isEmpty()) {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "An active season already exists for game " + request.game()
            ));
        }

        Season season = Season.builder()
                .name(request.name())
                .game(request.game())
                .type(request.type())
                .startsAt(request.startsAt())
                .endsAt(request.endsAt())
                .status(SeasonStatus.UPCOMING)
                .build();
        seasonRepository.save(season);

        return ResponseEntity.ok(SeasonResponse.from(season));
    }

    @PatchMapping("/{seasonId}/activate")
    public ResponseEntity<?> activateSeason(@PathVariable UUID seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("Season not found: " + seasonId));

        if (season.getStatus() != SeasonStatus.UPCOMING) {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "Cannot activate season with status " + season.getStatus()
            ));
        }

        var existingActive = seasonRepository.findByGameAndStatus(season.getGame(), SeasonStatus.ACTIVE);
        if (!existingActive.isEmpty()) {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "An active season already exists for game " + season.getGame()
            ));
        }

        season.setStatus(SeasonStatus.ACTIVE);
        seasonRepository.save(season);

        return ResponseEntity.ok(SeasonResponse.from(season));
    }

    @PatchMapping("/{seasonId}/close")
    public ResponseEntity<?> closeSeason(@PathVariable UUID seasonId) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new IllegalArgumentException("Season not found: " + seasonId));

        if (season.getStatus() != SeasonStatus.ACTIVE) {
            return ResponseEntity.status(409).body(Map.of(
                    "message", "Cannot close season with status " + season.getStatus()
            ));
        }

        season.setStatus(SeasonStatus.CLOSED);
        seasonRepository.save(season);

        leaderboardService.computeAndFinalise(seasonId);

        return ResponseEntity.ok(Map.of("status", "closed", "seasonId", seasonId));
    }

    @PostMapping("/{seasonId}/rewards/{rankPosition}/mark-claimed")
    public ResponseEntity<?> markRewardClaimed(@PathVariable UUID seasonId, @PathVariable Integer rankPosition) {
        Reward reward = rewardRepository.findBySeasonIdAndRankPosition(seasonId, rankPosition)
                .orElseThrow(() -> new IllegalArgumentException("Reward not found for rank " + rankPosition));
        User admin = currentUser();

        reward.setClaimed(true);
        rewardRepository.save(reward);

        auditLogRepository.save(AuditLog.builder()
                .actorUser(admin)
                .action("REWARD_CLAIMED")
                .entityType("Reward")
                .entityId(reward.getId().toString())
                .details("{\"seasonId\":\"" + seasonId + "\",\"rankPosition\":" + rankPosition + "}")
                .build());

        return ResponseEntity.ok(Map.of("status", "claimed"));
    }

    private User currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof JwtUser jwtUser)) {
            throw new IllegalStateException("Unauthorized");
        }
        return userRepository.findById(jwtUser.id())
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}
