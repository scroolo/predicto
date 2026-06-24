package com.predicto.season;

import com.predicto.common.enums.SeasonStatus;
import com.predicto.season.dto.LeaderboardEntryResponse;
import com.predicto.season.dto.RewardResponse;
import com.predicto.season.dto.SeasonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class SeasonController {

    private final SeasonRepository seasonRepository;
    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final RewardRepository rewardRepository;

    @GetMapping("/api/seasons")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SeasonResponse>> getAllSeasons() {
        var seasons = seasonRepository.findAll().stream()
                .sorted(Comparator.comparing(Season::getStartsAt).reversed())
                .map(SeasonResponse::from)
                .toList();
        return ResponseEntity.ok(seasons);
    }

    @GetMapping("/api/seasons/active")
    @Transactional(readOnly = true)
    public ResponseEntity<List<SeasonResponse>> getActiveSeasons() {
        var seasons = seasonRepository.findByStatus(SeasonStatus.ACTIVE).stream()
                .map(SeasonResponse::from)
                .toList();
        return ResponseEntity.ok(seasons);
    }

    @GetMapping("/api/seasons/{seasonId}/leaderboard")
    @Transactional(readOnly = true)
    public ResponseEntity<List<LeaderboardEntryResponse>> getLeaderboard(@PathVariable UUID seasonId) {
        var entries = leaderboardEntryRepository.findBySeasonIdOrderByRankPositionAsc(seasonId).stream()
                .map(LeaderboardEntryResponse::from)
                .toList();
        return ResponseEntity.ok(entries);
    }

    @GetMapping("/api/seasons/{seasonId}/rewards")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RewardResponse>> getRewards(@PathVariable UUID seasonId) {
        var rewards = rewardRepository.findBySeasonIdOrderByRankPositionAsc(seasonId).stream()
                .map(RewardResponse::from)
                .toList();
        return ResponseEntity.ok(rewards);
    }
}
