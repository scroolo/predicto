package com.predicto.season;

import com.predicto.auth.User;
import com.predicto.betting.Bet;
import com.predicto.betting.BetRepository;
import com.predicto.common.enums.BetStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaderboardService {

    private final LeaderboardEntryRepository leaderboardEntryRepository;
    private final RewardRepository rewardRepository;
    private final BetRepository betRepository;
    private final SeasonRepository seasonRepository;

    @Transactional
    public void recompute(UUID seasonId) {
        Season season = seasonRepository.findById(seasonId).orElse(null);
        if (season == null) return;

        List<Bet> settledBets = betRepository.findBySeasonId(seasonId).stream()
                .filter(b -> b.getStatus() == BetStatus.WON || b.getStatus() == BetStatus.LOST)
                .toList();

        Map<UUID, List<Bet>> betsByUser = settledBets.stream()
                .collect(Collectors.groupingBy(b -> b.getUser().getId()));

        leaderboardEntryRepository.deleteBySeasonId(seasonId);
        leaderboardEntryRepository.flush();

        if (betsByUser.isEmpty()) return;

        Set<UUID> allUserIds = new HashSet<>(betsByUser.keySet());

        List<LeaderboardEntry> entries = new ArrayList<>();

        for (UUID userId : allUserIds) {
            List<Bet> userBets = betsByUser.getOrDefault(userId, Collections.emptyList());
            int totalPoints = userBets.stream().mapToInt(b -> b.getPointsAwarded() != null ? b.getPointsAwarded() : 0).sum();
            int correctPicks = 0;
            int mvpCorrect = 0;
            int scoreCorrect = 0;

            for (Bet bet : userBets) {
                var match = bet.getMatch();
                if (bet.getWinnerTeam() != null && match.getResultWinnerTeam() != null
                        && bet.getWinnerTeam().getId().equals(match.getResultWinnerTeam().getId())) {
                    correctPicks++;
                }
                if (bet.getMvpPlayer() != null && match.getResultMvpPlayer() != null
                        && bet.getMvpPlayer().getId().equals(match.getResultMvpPlayer().getId())) {
                    mvpCorrect++;
                }
                if (bet.getExactScore() != null && match.getResultScore() != null
                        && bet.getExactScore().equals(match.getResultScore())) {
                    scoreCorrect++;
                }
            }

            User user = userBets.get(0).getUser();

            LeaderboardEntry lb = LeaderboardEntry.builder()
                    .season(season)
                    .user(user)
                    .points(totalPoints)
                    .correctPicks(correctPicks)
                    .mvpCorrect(mvpCorrect)
                    .scoreCorrect(scoreCorrect)
                    .computedAt(OffsetDateTime.now())
                    .build();
            entries.add(lb);
        }

        entries.sort((a, b) -> {
            int cmp = Integer.compare(b.getPoints(), a.getPoints());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.getCorrectPicks(), a.getCorrectPicks());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.getMvpCorrect(), a.getMvpCorrect());
            if (cmp != 0) return cmp;
            return Integer.compare(b.getScoreCorrect(), a.getScoreCorrect());
        });

        int currentRank = 0;
        Integer previousPoints = null;
        Integer previousCorrectPicks = null;
        Integer previousMvpCorrect = null;
        Integer previousScoreCorrect = null;

        for (LeaderboardEntry lb : entries) {
            if (previousPoints == null
                    || !lb.getPoints().equals(previousPoints)
                    || !lb.getCorrectPicks().equals(previousCorrectPicks)
                    || !lb.getMvpCorrect().equals(previousMvpCorrect)
                    || !lb.getScoreCorrect().equals(previousScoreCorrect)) {
                currentRank++;
            }
            lb.setRankPosition(currentRank);
            previousPoints = lb.getPoints();
            previousCorrectPicks = lb.getCorrectPicks();
            previousMvpCorrect = lb.getMvpCorrect();
            previousScoreCorrect = lb.getScoreCorrect();
        }

        leaderboardEntryRepository.saveAll(entries);
        log.info("Recomputed leaderboard for season {}: {} entries", seasonId, entries.size());
    }

    @Transactional
    public void computeAndFinalise(UUID seasonId) {
        recompute(seasonId);

        Season season = seasonRepository.findById(seasonId).orElse(null);
        if (season == null) return;

        List<LeaderboardEntry> entries = leaderboardEntryRepository.findBySeasonIdOrderByRankPositionAsc(seasonId);

        for (LeaderboardEntry entry : entries) {
            if (entry.getRankPosition() == null || entry.getRankPosition() > 5) continue;
            if (rewardRepository.findBySeasonIdAndRankPosition(seasonId, entry.getRankPosition()).isPresent()) {
                continue;
            }
            Reward reward = Reward.builder()
                    .season(season)
                    .rankPosition(entry.getRankPosition())
                    .user(entry.getUser())
                    .description("Top " + entry.getRankPosition() + " - " + season.getName())
                    .claimed(false)
                    .build();
            rewardRepository.save(reward);
        }

        log.info("Finalised season {} with rewards", seasonId);
    }
}
